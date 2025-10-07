package cleveyxw

import cleveyxw.model.{Difficulty, Player}
import cleveyxw.util.Database
import cleveyxw.util.UIUtils.styledAlert
import cleveyxw.view.AboutController
import javafx.fxml.FXMLLoader
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.Includes.*
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{Alert, ChoiceDialog}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.image.Image
import scalafx.stage.Modality.ApplicationModal
import scalafx.stage.Stage

import java.net.URL

object MainApp extends JFXApp3:
  // Setup database
  Database.setupDB()

  var rootPane: Option[javafx.scene.layout.BorderPane] = None
  var cssResource = getClass.getResource("view/style.css") // Load CSS styling
  val playerData: ObservableBuffer[Player] = ObservableBuffer() // Empty

  // Assign all players into playerData array
  playerData ++= Player.getAllPlayers

  // Store the current logged-in player
  var currentPlayer: Option[Player] = None
  // Game status
  private var isGameRunning: Boolean = false
  // Store selected diffculty
  var selectedDifficulty: Option[Difficulty] = None

  // Start the program and load the RootLayout together
  override def start(): Unit = {
    val rootLayoutResource: URL = getClass.getResource("/cleveyxw/view/RootLayout.fxml")
    val loader = new FXMLLoader(rootLayoutResource)
    val rootLayout = loader.load[javafx.scene.layout.BorderPane]()
    rootPane = Option(loader.getRoot[javafx.scene.layout.BorderPane]()) //Initialize
    stage = new PrimaryStage():
      title = "Hunger Defiance"
      icons += new Image(getClass.getResource("/images/cornmaster.png").toExternalForm)
      resizable = false
      scene = new Scene():
        root = rootLayout
        stylesheets = Seq(cssResource.toExternalForm)
    showLogin() // First window to show
  }

  // Function to disable Navigation Menu access if a Player is not logged in or in a game
  def checkAccess(feature: String): Boolean =
    // Player is not logged in
    if currentPlayer.isEmpty then
      styledAlert(new Alert(AlertType.Warning) {
        initOwner(stage)
        title = "Login Required"
        headerText = s"$feature Unavailable"
        contentText = "Please log in to access this feature."
      }).showAndWait()
      showLogin()
      false
    // Player is currently playing a game
    else if isGameRunning && feature != "Game" then
      styledAlert(new Alert(AlertType.Warning) {
        initOwner(stage)
        title = "Action Blocked"
        headerText = s"$feature Unavailable"
        contentText = "You cannot access this feature while in a game."
      }).showAndWait()
      false
    else true

  // Function to load the Login page
  def showLogin(): Unit =
    val login = getClass.getResource("/cleveyxw/view/Login.fxml")
    val loader = new FXMLLoader(login)
    val pane = loader.load[javafx.scene.layout.AnchorPane]()
    rootPane.foreach(_.setCenter(pane))

  // Function to load the Signup page
  def showSignup(): Unit =
    val signup = getClass.getResource("/cleveyxw/view/Signup.fxml")
    val loader = new FXMLLoader(signup)
    val pane = loader.load[javafx.scene.layout.AnchorPane]()
    rootPane.foreach(_.setCenter(pane))

  // Function to load the MainMenu page
  def showMainMenu(): Unit =
    if checkAccess("Main Menu") then
      val mainmenu = getClass.getResource("/cleveyxw/view/MainMenu.fxml")
      val loader = new FXMLLoader(mainmenu)
      val pane = loader.load[javafx.scene.layout.AnchorPane]()
      rootPane.foreach(_.setCenter(pane))

  // Function to load the Tutorial page
  def showTutorial(): Unit =
    if checkAccess("Tutorial") then
      val tutorial = getClass.getResource("/cleveyxw/view/Tutorial.fxml")
      val loader = new FXMLLoader(tutorial)
      val pane = loader.load[javafx.scene.layout.AnchorPane]()
      rootPane.foreach(_.setCenter(pane))

  // Function to select game difficulty settings
  def showDifficultySelection(requireChoice: Boolean = false): Unit =
    val choices = Seq("Easy", "Normal", "Hard")
    val dialog = new ChoiceDialog(defaultChoice = "Normal", choices):
      title = "Select Difficulty"
      headerText = "Choose your difficulty"
      contentText = "Difficulty:"

    val dialogPane = dialog.getDialogPane
    dialogPane.getStylesheets.add(
      getClass.getResource("/cleveyxw/view/style.css").toExternalForm
    )
    dialogPane.getStyleClass.add("custom-alert")

    // Remove cancel button (Only applies when restart game)
    if requireChoice then
      dialog.dialogPane().getButtonTypes.remove(javafx.scene.control.ButtonType.CANCEL)

      // Disable [X] button
      val stage = dialog.getDialogPane.getScene.getWindow
      stage.addEventFilter(
        javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST,
        (e: javafx.stage.WindowEvent) => e.consume()
      )

    val result = dialog.showAndWait()
    result match
      case Some(selected) =>
        val difficulty = selected match
          case "Easy" => Difficulty.Easy
          case "Normal" => Difficulty.Normal
          case "Hard" => Difficulty.Hard
        println(s"Player selected difficulty: $difficulty")
        startGame(difficulty) // Start the game with the selected difficulty
      case None =>
        if !requireChoice then
          println("Player cancelled difficulty selection.")
        else
          // If somehow None returned when cancel is removed, Normal difficulty will set as default
          startGame(Difficulty.Normal)

  // Function to load the Game page
  private def startGame(difficulty: Difficulty): Unit =
    if checkAccess("Game") then
      isGameRunning = true
      selectedDifficulty = Some(difficulty) // Store chosen difficulty

      val startGame = getClass.getResource("/cleveyxw/view/Game.fxml")
      val loader = new FXMLLoader(startGame)
      val pane = loader.load[javafx.scene.layout.AnchorPane]()

      // Pass the selected difficulty into the GameController
      val controller = loader.getController[cleveyxw.view.GameController]()
      controller.difficulty = difficulty
      controller.startGame()

      rootPane.foreach(_.setCenter(pane))

  // Function to end the game
  def endGame(): Unit =
    isGameRunning = false
    selectedDifficulty = None // Reset selected difficulty when game ends
    showMainMenu()

  // Function to load Leaderboards page
  def showLeaderboards(): Unit =
    if checkAccess("Leaderboards") then
      val leaderboards = getClass.getResource("/cleveyxw/view/Leaderboards.fxml")
      val loader = new FXMLLoader(leaderboards)
      val pane = loader.load[javafx.scene.layout.AnchorPane]()
      rootPane.foreach(_.setCenter(pane))

  // Function to load About page
  def showAbout(): Boolean =
    val about = getClass.getResource("/cleveyxw/view/About.fxml")
    val loader = new FXMLLoader(about)
    loader.load()
    val pane = loader.getRoot[javafx.scene.layout.AnchorPane]()
    val mywindow = new Stage():
      initOwner(stage)
      initModality(ApplicationModal)
      title = "About"
      resizable = false
      scene = new Scene():
        root = pane
        stylesheets = Seq(cssResource.toExternalForm)
    val ctrl = loader.getController[AboutController]()
    ctrl.stage = Option(mywindow)
    mywindow.showAndWait() // Pop up a window
    ctrl.okClicked
