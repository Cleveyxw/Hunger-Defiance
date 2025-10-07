package cleveyxw.model

import scalafx.beans.property.{StringProperty, IntegerProperty, ObjectProperty}
import cleveyxw.util.Database
import scalikejdbc._
import scala.util.{ Try, Success, Failure }

// Player class
class Player (val playerNameS : String, val passwordS : String) extends Database :
  def this()     = this(null, null)
  var playerName  = new StringProperty(playerNameS)
  var password   = new StringProperty(passwordS)
  var score = IntegerProperty(0)
  var difficulty = StringProperty("None")

  // Function to save a new Player into the database
  def save(): Try[Int] =
    if !isExist then
      Try(DB autoCommit { implicit session =>
        sql"""
        insert into player (playerName, password, score, difficulty)
        values (${playerName.value}, ${password.value}, ${score.value}, ${difficulty.value})
      """.update.apply()
      })
    else
      Failure(new Exception("Player with this name already exists"))

  // Function to delete a Player from the database
  def delete(): Try[Int] =
    if isExist then
      Try(DB autoCommit { implicit session =>
        sql"""
        delete from player
        where playerName = ${playerName.value}
      """.update.apply()
      })
    else
      throw new Exception("Player does not exist in Database")

  // Check if a Player already exists in the database
  def isExist: Boolean =
    DB readOnly { implicit session =>
      sql"""
      select * from player where playerName = ${playerName.value}
    """.map(_.string("playerName")).single.apply()
    } match
      case Some(_) => true
      case None    => false

  // Function to update the current score if new high score is found
  def updateScore(newScore: Int, selectedDifficulty: Difficulty): Try[Int] =
    if (newScore > score.value) then
      score.value = newScore // Update in-memory property
      difficulty.value = selectedDifficulty.toString
      if isExist then
        Try(DB autoCommit { implicit session =>
          sql"""
              update player
              set score = ${score.value}, difficulty = ${difficulty.value}
              where playerName = ${playerName.value}
            """.update.apply()
        })
      else
        Failure(new Exception("Player does not exist"))
    else
      // No update since newScore is not higher
      Success(0)

object Player extends Database:
  def apply (
              playerNameS : String,
              passwordS : String,
              scoreI : Int,
              difficultyS : String = "None"
            ) : Player =

    new Player(playerNameS, passwordS) :
      score.value = scoreI
      difficulty.value = difficultyS

  // Function to initialize the Player table in the database
  def initializeTable() =
    DB autoCommit { implicit session =>
      sql"""
    create table player (
      id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
      playerName varchar(64) unique,
      password varchar(64),
      score int,
      difficulty varchar(10)
    )
    """.execute.apply()
    }

  // Function to get all players from the database
  def getAllPlayers : List[Player] =
    DB readOnly { implicit session =>
      sql"select * from player".map(rs => Player(rs.string("playerName"),
        rs.string("password"),
        rs.int("score"), rs.string("difficulty") )).list.apply()
    }

