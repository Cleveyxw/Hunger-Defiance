package cleveyxw.util
import scalikejdbc.*
import cleveyxw.model.Player

trait Database :
  val derbyDriverClassname = "org.apache.derby.jdbc.EmbeddedDriver"
  val dbURL = "jdbc:derby:myDB;create=true;";
  // initialize JDBC driver & connection pool
  Class.forName(derbyDriverClassname)
  ConnectionPool.singleton(dbURL, "me", "mine")
  // ad-hoc session provider on the REPL
  given AutoSession = AutoSession

object Database extends Database :
  def setupDB() =
    if (!hasDBInitialize)
      Player.initializeTable()
  def hasDBInitialize : Boolean =
    DB getTable "Player" match
      case Some(x) => true
      case None => false
