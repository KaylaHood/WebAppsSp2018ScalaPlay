package controllers

import javax.inject._
import play.api.mvc._

/**
 * This controller registers a client with the sprite room
 * and establishes their websocket
 */
@Singleton
class SpriteRoomController @Inject()(cc: ControllerComponents) (implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {
  
  /**
   * Action that starts a client's session in the
   * sprite room
   */
  def index = Action {
    // TODO : establish websocket
    Ok(views.html.spriteroom("Welcome new user!"))
  }
}