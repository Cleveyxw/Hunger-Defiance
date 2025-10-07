package cleveyxw.view

import cleveyxw.MainApp
import cleveyxw.model.Player
import cleveyxw.util.UIUtils.styledAlert
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import javafx.fxml.FXML
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane

import scala.util.{Failure, Success}

@FXML
class SignupController():
  @FXML
  private var imageView: ImageView = _
  @FXML
  private var anchorPane: AnchorPane = _
  @FXML
  private var playerNameField: javafx.scene.control.TextField = null
  @FXML
  private var passwordField: javafx.scene.control.PasswordField = null

  @FXML
  def initialize(): Unit =
    // Bind ImageView size to AnchorPane size
    imageView.fitWidthProperty().bind(anchorPane.widthProperty())
    imageView.fitHeightProperty().bind(anchorPane.heightProperty())
  
  // Function to handle player registration
  @FXML
  def handleSignup(): Unit =
    val username = playerNameField.getText.trim
    val password = passwordField.getText.trim

    // Error handling if username or password is empty
    if username.isEmpty || password.isEmpty then
      styledAlert(new Alert(Alert.AlertType.Error) {
        initOwner(MainApp.stage)
        title = "Invalid Input"
        headerText = "Missing Information"
        contentText = "Please enter both a username and a password."
      }).showAndWait()

    // Error handling if entered username starts with a number
    else if username.headOption.exists(_.isDigit) then
      styledAlert(new Alert(AlertType.Error) {
        initOwner(MainApp.stage)
        title = "Invalid Username"
        headerText = "Invalid Player Name"
        contentText = "Player names cannot start with a number."
      }).showAndWait()

    // Error handling if password length exceeds 12 characters
    else if password.length > 12 then
      styledAlert(new Alert(AlertType.Error) {
        initOwner(MainApp.stage)
        title = "Invalid Password"
        headerText = "Password Too Long"
        contentText = "Passwords must not exceed 12 characters."
      }).showAndWait()

    // Create Player
    else
      val player = new Player(username, password)

      // Player details are stored into the database
      player.save() match
        case Success(_) =>
          styledAlert(new Alert(Alert.AlertType.Information) {
            initOwner(MainApp.stage)
            title = "Signup Successful"
            headerText = "Account Created"
            contentText = "Your account has been created successfully!"
          }).showAndWait()
          MainApp.showLogin()

        // Error handling where entered username already existed in the database
        // Each player has a unique username
        case Failure(ex) =>
          styledAlert(new Alert(Alert.AlertType.Error) {
            initOwner(MainApp.stage)
            title = "Signup Failed"
            headerText = "The username '" + username + "' is already in use. Please choose another one."
            contentText = ex.getMessage
          }).showAndWait()

  // Function to direct to login page
  @FXML
  def handleLogin(): Unit =
    MainApp.showLogin()
