package cleveyxw.view

import cleveyxw.MainApp
import cleveyxw.model.Player
import cleveyxw.util.UIUtils.styledAlert
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import javafx.fxml.FXML
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import scalafx.application.Platform
import scalikejdbc.*

@FXML
class LoginController():
  @FXML
  private var imageView: ImageView = _
  @FXML
  private var leftAnchorPane: AnchorPane = _
  @FXML
  private var rightAnchorPane: AnchorPane = _
  @FXML
  private var splitPane: javafx.scene.control.SplitPane = _
  @FXML
  private var playerNameField: javafx.scene.control.TextField = null
  @FXML
  private var passwordField: javafx.scene.control.PasswordField = null

  @FXML
  def initialize(): Unit =
    // Bind ImageView size to AnchorPane size
    imageView.fitWidthProperty().bind(leftAnchorPane.widthProperty())
    imageView.fitHeightProperty().bind(leftAnchorPane.heightProperty())
    
    Platform.runLater(() =>
      splitPane.lookupAll(".split-pane-divider").forEach(divider =>
      divider.setMouseTransparent(true) // disables mouse interaction
      )
    )

  // Function to handle player login
  @FXML
  def handleLogin(): Unit =
    val username = playerNameField.getText.trim
    val password = passwordField.getText.trim

    // Error handling if username or password is empty
    if username.isEmpty || password.isEmpty then
      styledAlert(new Alert(AlertType.Error) {
        initOwner(MainApp.stage)
        title = "Login Failed"
        headerText = "Missing Information"
        contentText = "Please enter both a username and a password."
      }).showAndWait()
    else

      // Check if player exists in the database
      val playerExists = DB readOnly { implicit session =>
        sql"select count(*) from player where playerName = $username"
          .map(_.int(1)).single.apply().getOrElse(0) > 0
      }

      // Player does not exist
      if !playerExists then
        styledAlert(new Alert(AlertType.Error) {
          initOwner(MainApp.stage)
          title = "Login Failed"
          headerText = "Player Not Found"
          contentText = s"No player with username '$username' exists."
        }).showAndWait()

      else
        // Try to find player in DB
        val playerOpt = DB readOnly { implicit session =>
          sql"""
            select * from player
            where playerName = $username and password = $password
          """.map(rs =>
            Player(
              rs.string("playerName"),
              rs.string("password"),
              rs.int("score")
            )
          ).single.apply()
        }

        playerOpt match
          // Login successful
          case Some(player) =>
            MainApp.currentPlayer = Some(player)   // <-- Store logged-in player

            styledAlert(new Alert(AlertType.Information) {
              initOwner(MainApp.stage)
              title = "Login Successful"
              headerText = s"Welcome, ${player.playerName.value}!"
              contentText = "Login Successful!"
            }).showAndWait()

            MainApp.showMainMenu()

          // Wrong password
          case None =>
            styledAlert(new Alert(AlertType.Error) {
              initOwner(MainApp.stage)
              title = "Login Failed"
              headerText = "Invalid Credentials"
              contentText = "The password you entered is incorrect."
            }).showAndWait()

  // Function to direct to sign up page
  @FXML
  def handleSignup(): Unit =
    MainApp.showSignup()
