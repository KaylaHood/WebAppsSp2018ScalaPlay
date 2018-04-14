package models

object Tables extends {
  val profile = slick.jdbc.MySQLProfile
  import profile.api._
  
  class Tasks(tag: Tag) extends Table[Task](tag, "tasks") {
    def taskId = column[Int]("taskId", O.PrimaryKey, O.AutoInc)
    def ownerId = column[Int]("ownerId")
    def title = column[String]("title")
    def desc = column[String]("desc", O.Default(""))
    def dueDate = column[java.sql.Timestamp]("dueDate")
    def * = (taskId.?, ownerId, title, desc, dueDate) <> (Task.tupled, Task.unapply)
  }
  val tasks = TableQuery[Tasks]
  
  class TaskUsers(tag: Tag) extends Table[TaskUser](tag, "taskUsers") {
    def userId = column[Int]("userId", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username")
    def password = column[String]("password")
    def * = (userId.? , username, password) <> (TaskUser.tupled, TaskUser.unapply)
  }
  val taskUsers = TableQuery[TaskUsers]
}