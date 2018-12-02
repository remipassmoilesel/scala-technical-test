package starsWatch

import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger
import play.api.libs.json._
import services.GithubStatsService
import starsWatch.StarWatcherActor.StartWatch
import starsWatch.WsClientActor.{ClientError, Subscribe, Unsubscribe}

import scala.collection.mutable

object WsClientActor {

  def props(clientRef: ActorRef, githubStatsService: GithubStatsService): Props = Props(new WsClientActor(clientRef, githubStatsService))

  final case class Subscribe(repository: String, intervalSec: Long)

  final case class Unsubscribe(repository: String)

  final case class ClientError(message: String)

  implicit val subscribeReads: Reads[Subscribe] = Json.reads[Subscribe]
  implicit val unsubscribeReads: Reads[Unsubscribe] = Json.reads[Unsubscribe]
  implicit val clientErrorWrites: Writes[ClientError] = Json.writes[ClientError]

}

class WsClientActor(clientRef: ActorRef, githubStatsService: GithubStatsService) extends Actor {

  private val clientSubscriptions = mutable.Map[String, ActorRef]()

  override def receive: Receive = {

    case Subscribe(repository, intervalSec) =>
      onSubscribe(repository, intervalSec)

    case Unsubscribe(repository) =>
      onUnsubscribe(repository)

    case ClientError(message) =>
      clientRef ! Json.toJson(ClientError(message)).toString()

    case msg: String =>
      self ! rawMessageToActorMessage(msg)

  }

  private def onSubscribe(repository: String, watchTimeSec: Long): Unit = {
    Logger.info(s"Subscribing to $repository with time of $watchTimeSec")

    if (!clientSubscriptions.isDefinedAt(repository)) {
      val childRef = context.actorOf(StarWatcherActor.props(clientRef, githubStatsService))
      clientSubscriptions.put(repository, childRef)
      childRef ! StartWatch(repository, watchTimeSec)
    } else {
      self ! ClientError(s"You are already subscribed to $repository")
    }

  }

  private def onUnsubscribe(repository: String): Unit = {
    Logger.info(s"Unsubscribing to $repository")

    if (clientSubscriptions.isDefinedAt(repository)) {
      val childRef = clientSubscriptions(repository)
      context stop childRef
      clientSubscriptions.remove(repository)
    } else {
      self ! ClientError(s"You are note subscribed to $repository")
    }

  }

  private def rawMessageToActorMessage(rawMessage: String): Any = {
    Logger.info(s"Received message from ws client: $rawMessage")

    try {
      parseRawMessage(rawMessage)
    } catch {
      case e: Exception =>
        Logger.error(s"Error while parsing message: $e", e)
        ClientError(s"Unexpected message: $rawMessage")
    }
  }

  private def parseRawMessage(rawMessage: String): Any = {

    val rawJson = Json.parse(rawMessage).as[JsObject]
    val action: Option[String] = rawJson.value.getOrElse("action", JsString("undefined")).asOpt[String]

    val parsedMessage = action match {

      case Some("subscribe") =>
        Json.fromJson[Subscribe](rawJson)
          .getOrElse(ClientError(s"Invalid subscribe message: ${rawJson.toString()}"))

      case Some("unsubscribe") =>
        Json.fromJson[Unsubscribe](rawJson)
          .getOrElse(ClientError(s"Invalid unsubscribe message: ${rawJson.toString()}"))

      case _ =>
        ClientError(s"Unexpected message: ${rawJson.toString()}")
    }

    parsedMessage
  }

  override def preStart(): Unit = Logger.info(s"New WsClientActor started")

  override def postStop(): Unit = Logger.info(s"WsClientActor actor stopped")

}
