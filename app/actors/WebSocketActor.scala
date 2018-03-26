package actors

import play.api.libs.json._
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

case class SocketMessage(var clientSprite: ClientSprite, var isSelf: Boolean)
case class ClientSprite(var id: Int, var xPos: Int, var yPos: Int)

class WebSocketActor(out: ActorRef, manager: ActorRef) extends Actor {
  import WebSocketActor._
  
  var actorSprite = new ClientSprite(0,0,0)
  
  println("Connecting new client actor...")
  actorSprite.id = WebSocketManager.getNextClientID
  manager.tell(WebSocketManager.NewClient(self), self)
  
  def receive = {
    case clientSprite: ClientSprite =>
      actorSprite = clientSprite
      manager.tell(WebSocketManager.BroadcastMessage(clientSprite), self)
    case ClientUpdate(clientState) =>
      println("Client recieved ClientUpdate response from WebSocketManager. clientState = " + clientState)
      if(clientState.id == actorSprite.id) {
        out.tell(new SocketMessage(clientState,true), self)
      }
      else {
        out.tell(new SocketMessage(clientState,false), self)
      }
    case m =>
      println("Unknown message: " + m)
  }
}

object WebSocketActor {
  def props(out: ActorRef, manager: ActorRef) = Props(new WebSocketActor(out, manager))
  
  case class ClientUpdate(clientState: ClientSprite)
}