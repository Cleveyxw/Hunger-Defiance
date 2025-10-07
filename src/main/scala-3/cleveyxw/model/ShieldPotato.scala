package cleveyxw.model

// ShieldPotato class
class ShieldPotato(row: Int, col: Int)
  extends Crop(250, 0, "/images/shieldpotato.png", row, col) {

  // Does not attack enemies, is purely a defensive crop
  override def attack(enemies: Seq[Enemy]): Unit = {
  }
}
