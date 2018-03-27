package actors

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

class WebSocketManager extends Actor {
  import WebSocketManager._
  private var clients = List[ActorAndSprite]()
  
  def receive = {
    case RemoveActor(clientActor, clientId) =>
      removeClient(clientActor)
      clients.foreach(c => c.actor.tell(WebSocketActor.RemoveClient(clientId), self))
    case NewActor(clientActor, clientSprite) =>
      println("New client connected")
      clients ::= new ActorAndSprite(clientActor, clientSprite)
      clients.foreach(c => clientActor.tell(WebSocketActor.ClientUpdate(c.sprite), self))
    case BroadcastMessage(clientActor, clientSprite) =>
      println("Broadcasting message to all clients...")
      clients.foreach(c => c.actor.tell(WebSocketActor.ClientUpdate(clientSprite), self))
  }
  
  def removeClient(clientRef: ActorRef) = {
    println("Removing client from websocket pool...");
    clients = clients.filterNot(x => x.actor == clientRef)
  }

}

object WebSocketManager {
  private var nextClientID = 0
  
  def getNextClientID = {
    var id = nextClientID
    nextClientID += 1
    id
  }
  
  def props = Props[WebSocketManager]
  
  case class RemoveActor(clientActor: ActorRef, clientId: Int)
  case class NewActor(clientActor: ActorRef, clientSprite: SpriteState)
  case class BroadcastMessage(clientActor: ActorRef, clientSprite: SpriteState)
  
  case class ActorAndSprite(actor: ActorRef, sprite: SpriteState)
}