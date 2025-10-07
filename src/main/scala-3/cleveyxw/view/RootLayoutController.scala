package cleveyxw.view

import javafx.event.ActionEvent
import javafx.fxml.FXML
import cleveyxw.MainApp

@FXML
class RootLayoutController():
  // Function to close the program
  @FXML
  def handleClose(action: ActionEvent): Unit =
    System.exit(0)

  // Function to direct to the About page
  @FXML
  def handleAbout(action: ActionEvent): Unit =
    MainApp.showAbout()

  // Function to direct to the Main Menu page
  @FXML
  def handleMainMenu(action: ActionEvent): Unit =
    if MainApp.checkAccess("Main Menu") then
      MainApp.showMainMenu()

  // Function to direct to the Leaderboards page
  @FXML
  def handleLeaderboards(action: ActionEvent): Unit =
    if MainApp.checkAccess("Leaderboards") then
      MainApp.showLeaderboards()

  // Function to direct to the Tutorial page
  @FXML
  def handleTutorial(action: ActionEvent): Unit =
    if MainApp.checkAccess("Tutorial") then
      MainApp.showTutorial()
