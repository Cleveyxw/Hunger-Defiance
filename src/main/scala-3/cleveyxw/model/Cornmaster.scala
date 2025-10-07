package cleveyxw.model

// Cornmaster class
class Cornmaster(row: Int, col: Int)
  extends Crop(150, 15, "/images/cornmaster.png", row, col) {

  // Attacks the enemy in the next tile
  override def attack(enemies: Seq[Enemy]): Unit = {
    enemies.find(e => e.isAlive && e.row == row && e.col == col + 1).foreach { e =>
      e.takeDamage(damage)
      println(s"Cornmaster at ($row,$col) attacks enemy at (${e.row},${e.col}) for $damage damage! Enemy HP: ${e.health}")
    }
  }
}

