package watchStars

import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger
import services.GithubStatsService

object ProjectWatcherActor {
  def props(clientRef: ActorRef, githubStatsService: GithubStatsService): Props = Props(new WsClientActor(clientRef, githubStatsService))
}

class ProjectWatcherActor(clientRef: ActorRef) extends Actor {

  override def receive: Receive = {
    case msg => Logger.info(msg.toString)
  }

  override def preStart(): Unit = Logger.info(s"Websocket connection actor started")

  override def postStop(): Unit = Logger.info(s"Websocket connection actor stopped")

}
