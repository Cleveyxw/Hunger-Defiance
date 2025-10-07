package cleveyxw.model

import javafx.scene.image.{Image, ImageView}

// Enemy abstract class
abstract class Enemy(var health: Int, val damage: Int, val imagePath: String, val row: Int, var col: Int) {
  var isNew: Boolean = true
  val imageView: ImageView = new ImageView(new Image(getClass.getResourceAsStream(imagePath)))
  imageView.setFitWidth(80)
  imageView.setFitHeight(80)
  imageView.setPreserveRatio(true)

  // Function to check if Enemy is still alive
  def isAlive: Boolean = health > 0

  // Function to take damage and reduce health
  def takeDamage(amount: Int): Unit = {
    health -= amount
    println(s"${this.getClass.getSimpleName} at ($row,$col) takes $amount damage, HP left: $health")
  }
}

