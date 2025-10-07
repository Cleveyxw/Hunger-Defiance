package cleveyxw.view

import javafx.fxml.FXML
import cleveyxw.MainApp
import cleveyxw.util.UIUtils.styledAlert
import javafx.event.ActionEvent
import javafx.scene.control.Label
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType

@FXML
class MainMenuController():
  @FXML
  private var currentPlayer: Label = _

  @FXML
  def initialize(): Unit =
    // Display the logged-in player's name of the current session
    MainApp.currentPlayer match
      case Some(player) =>
        currentPlayer.setText(s"Current Player: ${player.playerName.value}")
      case None =>
        currentPlayer.setText("No player logged in")

  // Function to log out a player account
  @FXML
  def handleLogout(): Unit =
    // Clear the logged-in player
    MainApp.currentPlayer = None

    // Display log out successful popup
    styledAlert(new Alert(AlertType.Information) {
      initOwner(MainApp.stage)
      title = "Logout"
      headerText = "Logout Sucessful!"
      contentText = "See you next time!"
    }).showAndWait()

    // Go back to login screen
    MainApp.showLogin()

  // Function to direct to the Tutorial page
  @FXML
  def handleTutorial(): Unit =
    MainApp.showTutorial()

  // Function to start the game and proceed to game difficulty settings
  @FXML
  def handleStart(): Unit =
    MainApp.showDifficultySelection()

  // Function to direct to the Leaderboards page
  @FXML
  def handleLeaderboards(): Unit =
    MainApp.showLeaderboards()

  // Function to close the program
  @FXML
  def handleClose(action: ActionEvent): Unit =
    System.exit(0)