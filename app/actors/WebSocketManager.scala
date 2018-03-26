package actors

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

class WebSocketManager extends Actor {
  import WebSocketManager._
  private var clients = List[ActorRef]()
  
  def receive = {
    case RemoveActor(clientActor, clientId) =>
      removeClient(clientActor)
      clients.foreach(c => c.tell(WebSocketActor.RemoveClient(clientId), self))
    case NewActor(clientActor, clientSprite) =>
      println("New client connected")
      clients ::= clientActor
      clientActor.tell(WebSocketActor.ClientUpdate(clientSprite), self)
    case BroadcastMessage(clientSprite) =>
      println("Broadcasting message to all clients...")
      clients.foreach(c => c.tell(WebSocketActor.ClientUpdate(clientSprite), self))
  }
  
  def removeClient(clientRef: ActorRef) = {
      if(clients.contains(clientRef)) {
        println("Removing client from websocket pool...");
        clients = clients.filterNot(x => x == clientRef)
      }
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
  case class BroadcastMessage(clientSprite: SpriteState)
}