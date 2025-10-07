package cleveyxw.view

import cleveyxw.MainApp
import javafx.fxml.FXML

@FXML
class TutorialController():
  // Function to redirect back to main menu page
  @FXML
  def handleBack(): Unit =
    MainApp.showMainMenu()