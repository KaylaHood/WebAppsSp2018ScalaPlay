package models

import slick.jdbc.MySQLProfile.api._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

//NOTE: passwords are stored as plain text and that's BAD BAD BAD man
//but also I'm lazy and won't be spending time on this task to implement hashes/salts.

case class NewTaskUser(username: String, password: String)
case class TaskUser(userId: Option[Int], username: String, password: String)

object TaskUserQueries {
  import Tables._
  def findByCredentials(username: String, password: String, db: Database)(implicit ec: ExecutionContext):Future[Option[TaskUser]] = {
    db.run {
      taskUsers.filter(_.username === username).filter(_.password === password).result.headOption
    }
  }
  
  def findById(userId: Int, db: Database)(implicit ec: ExecutionContext):Future[Option[TaskUser]] = {
    db.run {
      taskUsers.filter(_.userId === userId).result.headOption
    }
  }
  
  def addTaskUser(ntu: NewTaskUser, db: Database)(implicit ec: ExecutionContext): Future[Option[TaskUser]] = {
    val existing = findByCredentials(ntu.username, ntu.password, db)
    existing.transformWith{userTry => 
      if(userTry.get.isDefined) {
        Future(None)
      }
      else {
        db.run{
          (taskUsers returning taskUsers.map(_.userId) into ((user,id) => Some(user.copy(userId=Some(id))))) += TaskUser(None, ntu.username, ntu.password)
        }
      }
    }
  }
}

object TaskUserUtils {
  import Tables._
  
  def taskUserFromString(str: String):TaskUser = {
    val splitCommas = str.split(',')
    val id = splitCommas(0).toInt
    val username = splitCommas(1)
    val password = splitCommas(2)
    new TaskUser(new Some(id),username,password)
  }
  
  def stringFromTaskUser(tu: TaskUser):String = {
    tu.userId.get.toString() ++ "," ++ tu.username.toString() ++ "," ++ tu.password.toString()
  }
}