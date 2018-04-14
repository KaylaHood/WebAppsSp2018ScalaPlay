package controllers

import javax.inject._
import play.api.mvc._

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider

import slick.jdbc.JdbcProfile
import slick.jdbc.JdbcCapabilities
import slick.jdbc.MySQLProfile.api._
import models._
import scala.concurrent.ExecutionContext
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms._
import scala.concurrent.Future

@Singleton
class TaskController @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, mcc: MessagesControllerComponents) (implicit ec: ExecutionContext, assetsFinder: AssetsFinder)
  extends MessagesAbstractController(mcc) with HasDatabaseConfigProvider[JdbcProfile] {
  
  var loggedInUser: Option[TaskUser] = None
  
  def getNewTaskForm = {
    Form(mapping(
      "ownerId" -> ignored((loggedInUser.get.userId).get),
      "title" -> nonEmptyText,
      "desc" -> text(minLength = 0, maxLength = 300),
      "dueDate" -> sqlTimestamp("MM-dd-yyyy hh:mm"))(NewTask.apply)(NewTask.unapply))
  }
      
  def allUserTasks = Action.async { implicit request =>
    val sessConnected = request.session.get("connected")
    if(sessConnected.isEmpty) {
      Future(Redirect(routes.TaskLoginController.index))
    }
    else {
      loggedInUser = Some(TaskUserUtils.taskUserFromString(sessConnected.get))
      val tasksFuture = TaskQueries.findByUser(loggedInUser.get.userId.get, db)
      tasksFuture.map(tasks => Ok(views.html.taskManager(tasks, loggedInUser.get.username, this.getNewTaskForm)))
    }
  }
  
  def addTask = Action.async { implicit request =>
    val sessConnected = request.session.get("connected")
    if(sessConnected.isEmpty) {
      Future(Redirect(routes.TaskLoginController.index))
    }
    else {
      loggedInUser = Some(TaskUserUtils.taskUserFromString(sessConnected.get))
      (this.getNewTaskForm).bindFromRequest().fold(
        formWithErrors => {
          val tasksFuture = TaskQueries.findByUser(loggedInUser.get.userId.get, db)
          tasksFuture.map(tasks => BadRequest(views.html.taskManager(tasks, loggedInUser.get.username, formWithErrors)))
        },
        newTask => {
          val addFuture = TaskQueries.addTask(newTask, db)
          addFuture.map { cnt =>
            if(cnt == 1) Redirect(routes.TaskController.allUserTasks).flashing("message" -> "New task added.")
            else Redirect(routes.TaskController.allUserTasks).flashing("error" -> "Failed to add task.")
          }
        })
    }
  }
  
  def removeTask(id: Int) = Action.async { implicit request =>
    val sessConnected = request.session.get("connected")
    if(sessConnected.isEmpty) {
      Future(Redirect(routes.TaskLoginController.index))
    }
    else {
      loggedInUser = Some(TaskUserUtils.taskUserFromString(sessConnected.get))
      val tasksFuture = TaskQueries.removeTask(id, db)
      tasksFuture.map { cnt =>
        if(cnt == 1) Redirect(routes.TaskController.allUserTasks).flashing("message" -> "Task removed.")
        else Redirect(routes.TaskController.allUserTasks).flashing("error" -> "Task to be removed could not be found.")
      }
    }
  }
}