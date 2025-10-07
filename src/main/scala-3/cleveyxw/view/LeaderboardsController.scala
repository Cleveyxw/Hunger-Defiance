package cleveyxw.view

import cleveyxw.MainApp
import cleveyxw.model.Player
import javafx.fxml.FXML
import javafx.scene.control.{TableView, TableColumn}
import scalafx.beans.property.ReadOnlyObjectWrapper
import scalafx.collections.ObservableBuffer

@FXML
class LeaderboardsController():
  @FXML
  private var leaderboardTableView: TableView[Player] = _
  @FXML
  private var playerTableColumn: TableColumn[Player, String] = _
  @FXML
  private var difficultyTableColumn: TableColumn[Player, String] = _
  @FXML
  private var scoreTableColumn: TableColumn[Player, Int] = _

  def initialize(): Unit =
    // Set up columns to read properties from Player
    // Player Name
    playerTableColumn.setCellValueFactory(cellData =>
      ReadOnlyObjectWrapper(cellData.getValue.playerName.value)
    )
    // Selected Difficulty
    difficultyTableColumn.setCellValueFactory(cellData =>
      ReadOnlyObjectWrapper(cellData.getValue.difficulty.value)
    )
    // Game Score
    scoreTableColumn.setCellValueFactory(cellData =>
      ReadOnlyObjectWrapper(cellData.getValue.score.value)
    )

    // Display all players and sort by the highest score in descending order
    val players = Player.getAllPlayers.sortBy(-_.score.value)
    leaderboardTableView.setItems(ObservableBuffer(players*))

  // Redirect back to the main menu page
  @FXML
  def handleBack(): Unit =
    MainApp.showMainMenu()
