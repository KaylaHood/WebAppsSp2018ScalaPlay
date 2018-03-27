package actors

import play.api.libs.json._
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

object SocketMessageTypes {
  val removeClient = 0 // for removing a sprite room client when they disconnect
  val updateClient = 1 // for adding and changing existing sprite room client members
  val initSelf = 2 // for setting this client's id upon first connection
  val nullMsg = 3 // for keeping port open, nothing important in these messages
}

case class SocketMessage(var messageType: Int, var clientSprite: SpriteState)
case class SpriteState(var id: Int, var xPos: Int, var yPos: Int)

class WebSocketActor(out: ActorRef, manager: ActorRef) extends Actor {
  import WebSocketActor._
  
  var actorSprite = new SpriteState(0,0,0)
  
  println("Connecting new client actor...")
  actorSprite.id = WebSocketManager.getNextClientID
  println("New client id: " + actorSprite.id)
  // tell manager that this actor has been connected
  manager.tell(WebSocketManager.NewActor(self, actorSprite), self)
  // send initialization message to client
  out.tell(new SocketMessage(SocketMessageTypes.initSelf, actorSprite), self)
  
  def receive = {
    case SocketMessage(3, clientSprite) =>
      println("Received null message")
      // do nothing
    case SocketMessage(1, clientSprite) =>
      // update actorSprite -- ignore id because sometimes the client gets that messed up
      actorSprite.xPos = clientSprite.xPos
      actorSprite.yPos = clientSprite.yPos
      manager.tell(WebSocketManager.BroadcastMessage(self, clientSprite), self)
    case ClientUpdate(clientState) =>
      println("Client recieved ClientUpdate response from WebSocketManager. clientState = " + clientState)
      if(clientState.id == actorSprite.id) {
        // do nothing
      }
      else {
        out.tell(new SocketMessage(SocketMessageTypes.updateClient, clientState), self)
      }
    case RemoveClient(id) =>
      println("Client has been told to remove the client with id: " + id)
      out.tell(new SocketMessage(SocketMessageTypes.removeClient, new SpriteState(id, 0, 0)), self)
    case m =>
      println("Unknown message: " + m)
  }
  
  override def postStop() = {
    println("Client disconnecting...")
    manager.tell(WebSocketManager.RemoveActor(self, actorSprite.id), self)
  }
}

object WebSocketActor {
  def props(out: ActorRef, manager: ActorRef) = Props(new WebSocketActor(out, manager))
  
  case class ClientUpdate(clientState: SpriteState)
  case class RemoveClient(clientId: Int)
}