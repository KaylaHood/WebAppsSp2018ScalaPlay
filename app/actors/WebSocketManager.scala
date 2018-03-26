package actors

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

class WebSocketManager extends Actor {
  import WebSocketManager._
  private var clients = List[ActorRef]()
  
  def receive = {
    case NewClient(clientActor) =>
      println("New client connected")
      clients ::= clientActor
    case BroadcastMessage(clientState) =>
      println("Broadcasting message to all clients...")
      clients.foreach(c => c.tell(WebSocketActor.ClientUpdate(clientState), self))
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
  
  case class NewClient(clientActor: ActorRef)
  case class BroadcastMessage(clientState: ClientSprite)
}