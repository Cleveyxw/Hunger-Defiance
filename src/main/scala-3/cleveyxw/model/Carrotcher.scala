package cleveyxw.model

// Carrotcher class
class Carrotcher(row: Int, col: Int)
  extends Crop(75, 2, "/images/carrotcher.png", row, col) {

  // Attacks the first enemy in a row
  override def attack(enemies: Seq[Enemy]): Unit = {
    enemies.filter(e => e.isAlive && e.row == row && e.col > col).sortBy(_.col).headOption.foreach { e =>
      e.takeDamage(damage)
      println(s"Carrotcher at ($row,$col) attacks enemy at (${e.row},${e.col}) for $damage damage! Enemy HP: ${e.health}")
    }
  }
}

