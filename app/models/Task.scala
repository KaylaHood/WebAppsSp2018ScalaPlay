package models

import slick.jdbc.MySQLProfile.api._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import java.time.OffsetDateTime

case class NewTask(ownerId: Int, title: String, desc: String, dueDate: java.sql.Timestamp)
case class Task(taskId: Option[Int], ownerId: Int, title: String, desc: String, dueDate: java.sql.Timestamp)

object TaskQueries {
  import Tables._
  def findByUser(ownerId: Int, db: Database)(implicit ec: ExecutionContext):Future[Seq[Task]] = {
    db.run {
      tasks.filter(_.ownerId === ownerId).result
    }
  }
  
  def findById(taskId: Int, db: Database)(implicit ec: ExecutionContext):Future[Option[Task]] = {
    db.run {
      tasks.filter(_.taskId === taskId).result.headOption
    }
  }
  
  def addTask(nt: NewTask, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run {
      tasks += Task(None, nt.ownerId, nt.title, nt.desc, nt.dueDate)
    }
  }
  
  def removeTask(taskId: Int, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    val q = tasks.filter(_.taskId === taskId)
    val action = q.delete
    db.run(action)
  }
}