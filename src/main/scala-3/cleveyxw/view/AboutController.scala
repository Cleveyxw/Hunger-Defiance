package cleveyxw.view

import javafx.event.ActionEvent
import javafx.fxml.FXML
import scalafx.stage.Stage

@FXML
class AboutController():
  // MODEL PROPERTY
  // STAGE PROPERTY
  var stage: Option[Stage] = None
  // RETURN PROPERTY
  var okClicked = false

  // Function to close the popup window
  @FXML
  def handleClose(action: ActionEvent): Unit =
    okClicked = true
    stage.foreach(_.close())

