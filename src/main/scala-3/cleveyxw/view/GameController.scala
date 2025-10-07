package cleveyxw.view

import cleveyxw.MainApp
import javafx.animation.{Animation, KeyFrame, Timeline}
import javafx.fxml.FXML
import javafx.scene.control.{Button, Label}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.{ClipboardContent, DragEvent, MouseEvent, TransferMode}
import javafx.scene.layout.{ColumnConstraints, GridPane, Pane, RowConstraints, VBox}
import javafx.scene.{Cursor, ImageCursor, Scene}
import javafx.util.Duration
import cleveyxw.model.*
import javafx.geometry.{Insets, Pos}
import scalafx.application.Platform
import javafx.stage.WindowEvent
import scalafx.stage.{Modality, Stage, StageStyle}
import scala.util.Random
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.*

class GameController():
  @FXML
  private var cornImageView: ImageView = _
  @FXML
  private var carrotImageView: ImageView = _
  @FXML
  private var potatoImageView: ImageView = _
  @FXML
  private var shovelImageView: ImageView = _
  @FXML
  private var gameGrid: GridPane = _
  @FXML
  private var resourceLabel: Label = _
  @FXML
  private var scoreLabel: Label = _
  @FXML
  private var horizontalSplitPane: javafx.scene.control.SplitPane = _
  @FXML
  private var verticalSplitPane: javafx.scene.control.SplitPane = _
  @FXML
  private var pauseButton: Button = _

  // Game state variables
  private var _difficulty: Difficulty = Difficulty.Normal // Normal Difficulty is set as default
  def difficulty: Difficulty = _difficulty
  def difficulty_=(d: Difficulty): Unit =
    _difficulty = d
    println(s"Difficulty set to $d")

  private var gamePaused: Boolean = false // Game paused Status
  private var gameOver: Boolean = false // Game over status
  private var resources: Int = 50 // Initial resource count
  private val cornCost = 50 // Cornmaster resource cost
  private val carrotCost = 25 // Carrotcher resource cost
  private val potatoCost = 30 // ShieldPotato resource cost
  private var selectedCropCost: Int = 0 // Cost of the currently selected crop
  private var shovelMode: Boolean = false // Shovel Mode status
  private var score: Int = 0 // Current game score

  // Game timers
  private val enemySpawnTimer = Timeline()
  private var resourceTimer: Timeline = _
  private var cropAttackTimer: Timeline = _
  private var enemyAttackTimer: Timeline = _
  private var enemyMoveTimer: Timeline = _

  // Random generator for spawning enemies
  private val random = new Random()

  // Game entities: Crops and Enemies
  private val crops = Array.ofDim[Crop](5, 9)
  private val activeEnemies = ArrayBuffer[Enemy]()

  // Function to handle pause and resume button
  @FXML
  private def onPauseButtonClicked(): Unit =
    if gamePaused then
      resumeGame()
    else
      pauseGame()
      showPausePopup()

  // Function to pause all game timers during game paused
  private def pauseGame(): Unit =
    if !gamePaused then
      println("Game paused.")
      gamePaused = true
      enemySpawnTimer.pause()
      if resourceTimer != null then resourceTimer.pause()
      if cropAttackTimer != null then cropAttackTimer.pause()
      if enemyAttackTimer != null then enemyAttackTimer.pause()
      if enemyMoveTimer != null then enemyMoveTimer.pause()

  // Function to resume all game timers when game is unpaused
  private def resumeGame(): Unit =
    if gamePaused then
      println("Game resumed.")
      gamePaused = false
      enemySpawnTimer.play()
      if resourceTimer != null then resourceTimer.play()
      if cropAttackTimer != null then cropAttackTimer.play()
      if enemyAttackTimer != null then enemyAttackTimer.play()
      if enemyMoveTimer != null then enemyMoveTimer.play()

  // Function to initialize and start game
  def startGame(): Unit =
    println(s"Starting game with difficulty: $difficulty")

    setupDragSource(cornImageView, cornCost)
    setupDragSource(carrotImageView, carrotCost)
    setupDragSource(potatoImageView, potatoCost)
    setupShovel()
    setupGridTiles()
    setupResourceTimer()
    setupEnemySpawner()
    setupCropsAttackLoop()
    setupEnemyAttackLoop()
    updateResourceDisplay()
    updateScoreDisplay()
    shovelImageView.getStyleClass.add("shovel-default")

    Platform.runLater(() =>
      List(horizontalSplitPane, verticalSplitPane).foreach { sp =>
        sp.lookupAll(".split-pane-divider").forEach(_.setMouseTransparent(true))
      }
    )

  // Function for Drag-and-Drop setup for deploying crops
  private def setupDragSource(source: ImageView, cost: Int): Unit =
    source.setOnDragDetected((event: MouseEvent) =>
      if !shovelMode && resources >= cost then
        selectedCropCost = cost
        val db = source.startDragAndDrop(TransferMode.COPY)
        val content = ClipboardContent()
        content.putImage(source.getImage)
        db.setContent(content)
      event.consume()
    )

  // Function for Shovel mode setup
  private def setupShovel(): Unit =
    shovelImageView.setOnMouseClicked(_ =>
      if !shovelMode then
        val stream = getClass.getResourceAsStream("/images/shovel_cursor.png")
        if stream != null then
          val shovelCursorImage = new Image(stream)
          val imageCursor = new ImageCursor(shovelCursorImage)
          // Shovel Mode ON: Mouse cursor becomes a shovel
          gameGrid.setCursor(imageCursor)
          shovelMode = true

          // Toggle CSS styles
          shovelImageView.getStyleClass.remove("shovel-default")
          if !shovelImageView.getStyleClass.contains("shovel-selected") then
            shovelImageView.getStyleClass.add("shovel-selected")

          println("Shovel mode activated!")
        else
          println("Could not load shovel_cursor.png. Cursor not changed.")
      else
        // Shovel Mode OFF: Mouse cursor becomes normal
        gameGrid.setCursor(Cursor.DEFAULT)
        shovelMode = false

        // Toggle CSS styles back
        shovelImageView.getStyleClass.remove("shovel-selected")
        if !shovelImageView.getStyleClass.contains("shovel-default") then
          shovelImageView.getStyleClass.add("shovel-default")

        println("Shovel mode deactivated.")
    )

  // Function to check if the tile is occupied by any crop or enemy
  private def isTileOccupied(row: Int, col: Int): Boolean =
    crops(row)(col) != null || activeEnemies.exists(e => e.row == row && e.col == col)

  // Function for grid and tile setup
  private def setupGridTiles(): Unit =
    val rows = 5
    val cols = difficulty match
      case Difficulty.Easy => 9
      case Difficulty.Normal => 7
      case Difficulty.Hard => 6

    // Clear old constraints
    gameGrid.getColumnConstraints.clear()
    gameGrid.getRowConstraints.clear()

    for _ <- 0 until cols do
      val cc = ColumnConstraints()
      cc.setPercentWidth(100.0 / cols)
      gameGrid.getColumnConstraints.add(cc)

    for _ <- 0 until rows do
      val rc = RowConstraints()
      rc.setPercentHeight(100.0 / rows)
      gameGrid.getRowConstraints.add(rc)

    // Rebuild grid with correct columns
    for row <- 0 until rows; col <- 0 until cols do
      val tile = new Pane()
      tile.setPrefSize(80, 80)
      tile.setStyle("-fx-border-color: gray; -fx-background-color: #e6ffe6;")

      // Dragging over crops behavior
      tile.setOnDragOver((event: DragEvent) =>
        if event.getDragboard.hasImage && !isTileOccupied(row, col) then
          event.acceptTransferModes(TransferMode.COPY)
          tile.setStyle("-fx-border-color: green; -fx-background-color: #f4fff0;")
        else if isTileOccupied(row, col) then
          tile.setStyle("-fx-border-color: red; -fx-background-color: #ffdddd;")
        event.consume()
      )

      tile.setOnDragExited((_: DragEvent) => tile.setStyle("-fx-border-color: gray; -fx-background-color: #e6ffe6;"))

      // Dropping crops behavior
      tile.setOnDragDropped((event: DragEvent) =>
        val db = event.getDragboard
        if db.hasImage && resources >= selectedCropCost && !isTileOccupied(row, col) then
          val crop: Crop = selectedCropCost match
            case `cornCost` => Cornmaster(row, col)
            case `carrotCost` => Carrotcher(row, col)
            case `potatoCost` => ShieldPotato(row, col)
          tile.getChildren.clear()
          tile.getChildren.add(crop.imageView)
          crops(row)(col) = crop
          resources -= selectedCropCost
          updateResourceDisplay()
          event.setDropCompleted(true)
        else event.setDropCompleted(false)
        event.consume()
      )

      // Shovel Mode removing behavior
      tile.setOnMouseClicked(event =>
        if shovelMode then
          val col = GridPane.getColumnIndex(tile)
          val row = GridPane.getRowIndex(tile)

          // Check if an enemy is present on current tile
          val hasEnemy = activeEnemies.exists(enemy => enemy.row == row && enemy.col == col)

          if hasEnemy then
            // Visual feedback that enemies can't be removed
            tile.setStyle("-fx-border-color: orange; -fx-background-color: #fff0f0;")
            val feedbackTimeline = new Timeline(KeyFrame(Duration.seconds(0.5)))
            feedbackTimeline.setOnFinished(_ =>
              tile.setStyle("-fx-border-color: gray; -fx-background-color: #e6ffe6;")
            )
            feedbackTimeline.play()
            println(s"Cannot remove enemy at ($row,$col) - shovel only works on plants!")
          else
            // Check for a crop in this tile
            Option(crops(row)(col)).foreach { crop =>
              tile.getChildren.clear()
              crops(row)(col) = null
              println(s"Plant removed from ($row,$col).")
            }
        event.consume()
      )

      gameGrid.add(tile, col, row)

    // Resize crops array to new column count
    for r <- crops.indices do
      crops(r) = new Array[Crop](cols)

  // Function for resource timer setup
  private def setupResourceTimer(): Unit =
    // Add 10 resources for every 5 seconds
    resourceTimer = Timeline(
      KeyFrame(Duration.seconds(5), _ =>
        if !gameOver then
          resources = Math.min(resources + 10, 150) // Cap max resource at 150
          updateResourceDisplay()
      )
    )
    resourceTimer.setCycleCount(Animation.INDEFINITE)
    resourceTimer.play()

  // Function for enemy spawner setup
  private def setupEnemySpawner(): Unit =
    val spawnInterval = difficulty match
      case Difficulty.Easy => 10 // Easy Difficulty - 10 seconds
      case Difficulty.Normal => 10 // Normal Difficulty - 10 seconds
      case Difficulty.Hard => 6 // Hard Difficulty - 6 seconds

    val lastCol = gameGrid.getColumnConstraints.size() - 1

    // Enemy spawn interval depends on the selected difficulty
    val keyFrame = KeyFrame(Duration.seconds(spawnInterval), _ =>
      val row = random.nextInt(5)
      val enemy = Seq(FoodWasteBlob(row, lastCol), PollutionCloud(row, lastCol))(random.nextInt(2))
      val spawnTile = getTile(row, lastCol)
      spawnTile.getChildren.clear()
      spawnTile.getChildren.add(enemy.imageView)
      println(s"${enemy.getClass.getSimpleName} spawned at ($row,$lastCol)")
      activeEnemies += enemy
    )
    enemySpawnTimer.getKeyFrames.clear()
    enemySpawnTimer.getKeyFrames.add(keyFrame)
    enemySpawnTimer.setCycleCount(Animation.INDEFINITE)
    enemySpawnTimer.play()

  // Function for Enemy attack and movement loop setup
  private def setupEnemyAttackLoop(): Unit =
    // Enemy attacks crops every 1 second
    enemyAttackTimer = Timeline(
      KeyFrame(Duration.seconds(1), _ =>
        activeEnemies.foreach { enemy =>
          val targetCol = enemy.col - 1
          if targetCol >= 0 then
            Option(crops(enemy.row)(targetCol)).foreach { crop =>
              crop.takeDamage(enemy.damage)
              println(s"${enemy.getClass.getSimpleName} attacks crop at (${crop.row},${crop.col}) for ${enemy.damage} damage!")
              if !crop.isAlive then
                println(s"Crop at (${crop.row},${crop.col}) destroyed!")
                getTile(crop.row, crop.col).getChildren.clear()
                crops(crop.row)(crop.col) = null
            }
        }
      )
    )
    enemyAttackTimer.setCycleCount(Animation.INDEFINITE)
    enemyAttackTimer.play()

    // Enemy moves every 5 seconds
    val moveTimer = Timeline(
      KeyFrame(Duration.seconds(5), _ =>
        activeEnemies.foreach { enemy =>
          if enemy.isNew then
            enemy.isNew = false  // First cycle: mark as no longer new, no movement
            println(s"${enemy.getClass.getSimpleName} at (${enemy.row},${enemy.col}) finished spawn delay")
          else
            val targetCol = enemy.col - 1

            // Check if enemy reached the leftmost column
            if targetCol < 0 then
              println(s"${enemy.getClass.getSimpleName} reached the end! GAME OVER!")
              endGame()
            else if crops(enemy.row)(targetCol) == null || !crops(enemy.row)(targetCol).isAlive then
              val currentTile = getTile(enemy.row, enemy.col)
              val nextTile = getTile(enemy.row, targetCol)
              currentTile.getChildren.remove(enemy.imageView)
              nextTile.getChildren.add(enemy.imageView)
              enemy.col = targetCol
              println(s"${enemy.getClass.getSimpleName} moved from (${enemy.row},${enemy.col + 1}) to (${enemy.row},${enemy.col})")
        }

        // Remove dead enemies
        activeEnemies.filterInPlace { enemy =>
          if enemy.health <= 0 then
            println(s"${enemy.getClass.getSimpleName} at (${enemy.row},${enemy.col}) has died!")
            getTile(enemy.row, enemy.col).getChildren.remove(enemy.imageView)
            addScore(10)
            false
          else true
        }
      )
    )
    moveTimer.setCycleCount(Animation.INDEFINITE)
    enemyMoveTimer = moveTimer
    enemyMoveTimer.play()

  // Function for Crops attack loop setup
  private def setupCropsAttackLoop(): Unit =
    // Crops attack every 1 second
    cropAttackTimer = Timeline(
      KeyFrame(Duration.seconds(1), _ =>
        for
          row <- crops.indices
          col <- crops(row).indices
          crop = crops(row)(col)
          if crop != null && crop.damage > 0
        do
          crop.attack(activeEnemies.toSeq)

            // Remove dead enemies after attack
            activeEnemies.filterInPlace { enemy =>
              if enemy.health <= 0 then
                println(s"${enemy.getClass.getSimpleName} at (${enemy.row},${enemy.col}) destroyed by crop!")
                getTile(enemy.row, enemy.col).getChildren.remove(enemy.imageView)
                addScore(10)
                false // remove from activeEnemies
              else true
            }
      )
    )
    cropAttackTimer.setCycleCount(Animation.INDEFINITE)
    cropAttackTimer.play()

  // Function to get a grid tile
  private def getTile(row: Int, col: Int): Pane =
    val nodes = gameGrid.getChildren.filtered(n =>
      Option(GridPane.getRowIndex(n)).getOrElse(0) == row &&
        Option(GridPane.getColumnIndex(n)).getOrElse(0) == col
    ).asScala
    nodes.headOption match
      case Some(node) => node.asInstanceOf[Pane]
      // Creates an emergency tile if tile is missing
      case None =>
        val emergencyTile = new Pane()
        emergencyTile.setPrefSize(80, 80)
        gameGrid.add(emergencyTile, col, row)
        emergencyTile

  // Function to display game over popup when the game ends
  private def showGameOverPopup(score: Int): Unit =
    val stage = new Stage()
    stage.initModality(Modality.ApplicationModal)
    stage.initStyle(StageStyle.Decorated) // keep title bar
    stage.setResizable(false) // Cannot resize
    stage.setTitle("Game Over")

    // Prevent closing via [X] button or ESC
    stage.setOnCloseRequest((e: WindowEvent) => e.consume())

    // Labels
    val titleLabel = new Label("GAME OVER!")
    titleLabel.getStyleClass.add("popup-title")

    val scoreLabel = new Label(s"Your final score is: $score")
    scoreLabel.getStyleClass.add("popup-score")

    // Buttons
    val restartButton = new Button("Restart")
    restartButton.getStyleClass.add("popup-button")

    val exitButton = new Button("Exit")
    exitButton.getStyleClass.add("popup-button")

    // Button actions
    restartButton.setOnAction(_ => {
      stage.close()
      MainApp.showDifficultySelection(requireChoice = true)
    })
    exitButton.setOnAction(_ => {
      stage.close()
      MainApp.endGame()
    })

    // Layout
    val vbox = new VBox(20, titleLabel, scoreLabel, restartButton, exitButton)
    vbox.setAlignment(Pos.CENTER)
    vbox.setPadding(Insets(30))
    vbox.getStyleClass.add("popup-root")

    // Scene with fixed size
    val scene = new Scene(vbox, 420, 260) // wider & taller
    scene.getStylesheets.add(getClass.getResource("/cleveyxw/view/style.css").toExternalForm)
    stage.setScene(scene)
    stage.show()


  // Function to end the game
  private def endGame(): Unit =
    if !gameOver then
      gameOver = true
      println("GAME OVER! Stop all actions.")
      // Pauses all game timers
      enemySpawnTimer.stop()
      if resourceTimer != null then resourceTimer.stop()
      if cropAttackTimer != null then cropAttackTimer.stop()
      if enemyAttackTimer != null then enemyAttackTimer.stop()
      if enemyMoveTimer != null then enemyMoveTimer.stop()

      // Update current player's score in DB
      MainApp.currentPlayer.foreach { player =>
        player.updateScore(score, difficulty) match
          case scala.util.Success(_) =>
            println(s"Score updated to $score with difficulty $difficulty for ${player.playerName.value}")
          case scala.util.Failure(e) =>
            println(s"Failed to update score: ${e.getMessage}")
      }

      showGameOverPopup(score)

  // Function to display and update the resource UI
  private def updateResourceDisplay(): Unit =
    if resourceLabel != null then resourceLabel.setText(s"Seeds: $resources")

  // Function to display and update the score UI
  private def updateScoreDisplay(): Unit =
    if scoreLabel != null then scoreLabel.setText(s"Score: $score")

  // Function to add scores
  private def addScore(points: Int): Unit =
    score += points
    updateScoreDisplay()

  // Function to display pause game popup during game pause
  private def showPausePopup(): Unit =
    val stage = new Stage()
    stage.initModality(Modality.ApplicationModal)
    stage.initStyle(StageStyle.Decorated)
    stage.setResizable(false)
    stage.setTitle("Game Paused")

    // Closing with [X] will resume the game
    stage.setOnCloseRequest((e: WindowEvent) =>
      resumeGame()
    )

    val titleLabel = new Label("GAME PAUSED")
    titleLabel.getStyleClass.add("popup-title")

    // Resume Button
    val resumeButton = new Button("Resume")
    resumeButton.getStyleClass.add("popup-button")
    resumeButton.setOnAction(_ =>
      stage.close()
      resumeGame()
    )

    // Exit Button
    val exitButton = new Button("Exit")
    exitButton.getStyleClass.add("popup-button")
    exitButton.setOnAction(_ =>
      stage.close()
      MainApp.endGame()
    )

    val vbox = new VBox(20, titleLabel, resumeButton, exitButton)
    vbox.setAlignment(Pos.CENTER)
    vbox.setPadding(Insets(30))
    vbox.getStyleClass.add("popup-root")

    val scene = new Scene(vbox, 400, 220)
    scene.getStylesheets.add(getClass.getResource("/cleveyxw/view/style.css").toExternalForm)
    stage.setScene(scene)
    stage.show()