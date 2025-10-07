package cleveyxw.util

import scalafx.scene.control.Alert

// Utility function to apply CSS for popup alerts
object UIUtils:
  def styledAlert(alert: Alert): Alert =
    val dialogPane = alert.dialogPane()
    dialogPane.getStylesheets.add(getClass.getResource("/cleveyxw/view/style.css").toExternalForm)
    dialogPane.getStyleClass.add("custom-alert")
    alert