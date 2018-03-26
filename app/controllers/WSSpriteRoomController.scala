package controllers

import play.api.libs.json._
import play.api.mvc._
import play.api.libs.streams.ActorFlow
import javax.inject.Inject
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.mvc.WebSocket.MessageFlowTransformer
import akka.actor._
import actors.WebSocketActor
import actors.WebSocketManager
import actors.ClientSprite
import actors.SocketMessage

class WSSpriteRoomController @Inject() (cc:ControllerComponents) (implicit system: ActorSystem, mat: Materializer, assetsFinder: AssetsFinder) extends AbstractController(cc) {
  implicit val clientSpriteFormat = Json.format[ClientSprite]
  implicit val socketMsgFormat = Json.format[SocketMessage]
  implicit val messageFlowTransformer = MessageFlowTransformer.jsonMessageFlowTransformer[ClientSprite, SocketMessage]
  
  val wsManager = system.actorOf(WebSocketManager.props)
  
  def index = Action { implicit request =>
    Ok(views.html.spriteroom("Welcome to the Sprite Room!"))
  }
  
  def socket = WebSocket.accept[ClientSprite, SocketMessage] { request =>
    ActorFlow.actorRef { out =>
      WebSocketActor.props(out, wsManager)
    }
  }
  
  
}