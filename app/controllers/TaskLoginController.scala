package controllers

import javax.inject._
import play.api.mvc._
import play.api.Logger

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
class TaskLoginController @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, mcc: MessagesControllerComponents) (implicit ec: ExecutionContext, assetsFinder: AssetsFinder)
  extends MessagesAbstractController(mcc) with HasDatabaseConfigProvider[JdbcProfile] {
  
  def getLoginForm = {
    Form(mapping(
      "username" -> nonEmptyText(minLength = 1, maxLength = 32),
      "password" -> nonEmptyText(minLength = 1, maxLength = 32))(NewTaskUser.apply)(NewTaskUser.unapply))
  }
  
  def index = Action { implicit request =>
    Ok(views.html.taskLogin(this.getLoginForm,this.getLoginForm))
  }
  
  def logout = Action { implicit request =>
    Ok(views.html.taskLogin(this.getLoginForm,this.getLoginForm)).withNewSession
  }

  def login = Action.async { implicit request =>
    (this.getLoginForm).bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.taskLogin(formWithErrors,this.getLoginForm)))
      },
      loginForm => {
        val loggedInUser = TaskUserQueries.findByCredentials(loginForm.username, loginForm.password, db)
        loggedInUser.map{user =>
          if(user.isDefined) {
            Redirect(routes.TaskController.allUserTasks).withSession("connected" -> TaskUserUtils.stringFromTaskUser(user.get))
          }
          else {
            BadRequest(views.html.taskLogin((this.getLoginForm).withGlobalError("Could not find the user with that username and password"),this.getLoginForm))
          }
        }
      })
  }
  
  def register = Action.async { implicit request =>
    (this.getLoginForm).bindFromRequest().fold(
      formWithErrors => {
        Logger.debug("Registration form had errors...")
        Future(BadRequest(views.html.taskLogin(this.getLoginForm, formWithErrors)))
      },
      registerForm => {
        Logger.debug("Before registering new user.")
        val registerUser = TaskUserQueries.addTaskUser(registerForm, db)
        registerUser.map{newUser => 
          if(newUser.isDefined) {
            Logger.debug("New user was created... redirecting")
            Redirect(routes.TaskController.allUserTasks).withSession("connected" -> TaskUserUtils.stringFromTaskUser(newUser.get))
          }
          else {
            Logger.debug("User already existed")
            BadRequest(views.html.taskLogin(this.getLoginForm, (this.getLoginForm).withGlobalError("There is already a user with that username and password")))
          }
        }
      })
  }
  
}