package githubStats

import akka.actor.{Actor, ActorRef, Props}
import githubStats.StarWatcherActor.StartWatch
import githubStats.WsClientActor.{Subscribe, Unsubscribe, WsClientError, WsClientMessage}
import play.api.Logger
import play.api.libs.json._

import scala.collection.mutable

object WsClientActor {

  def props(clientRef: ActorRef, starWatcherFactory: StarWatcherActorFactory): Props = Props(new WsClientActor(clientRef, starWatcherFactory))

  final case class Subscribe(repository: String, intervalSec: Long)

  final case class Unsubscribe(repository: String)

  final case class WsClientError(message: String)

  final case class WsClientMessage(message: String)

  implicit val subscribeReads: Reads[Subscribe] = Json.reads[Subscribe]
  implicit val unsubscribeReads: Reads[Unsubscribe] = Json.reads[Unsubscribe]

  implicit val clientErrorWrites: Writes[WsClientError] = Json.writes[WsClientError]
  implicit val clientMessageWrites: Writes[WsClientMessage] = Json.writes[WsClientMessage]

}

class WsClientActor(wsClientRef: ActorRef, starWatcherFactory: StarWatcherActorFactory) extends Actor {

  private val clientSubscriptions = mutable.Map[String, ActorRef]()

  override def receive: Receive = {

    case Subscribe(repository, intervalSec) =>
      onSubscribe(repository, intervalSec)

    case Unsubscribe(repository) =>
      onUnsubscribe(repository)

    case msg: String =>
      onWsClientMessage(msg)

  }

  private def onSubscribe(repository: String, watchTimeSec: Long): Unit = {
    Logger.info(s"Subscribing to $repository with time of $watchTimeSec")

    if (!clientSubscriptions.isDefinedAt(repository)) {
      val childRef = starWatcherFactory.newStarWatcher(context, wsClientRef)
      clientSubscriptions.put(repository, childRef)

      childRef ! StartWatch(repository, watchTimeSec)
      wsClientRef ! Json.toJson(WsClientMessage(s"Subscribed to $repository with interval of $watchTimeSec seconds")).toString()
    } else {
      wsClientRef ! Json.toJson(WsClientError(s"You are already subscribed to $repository")).toString()
    }

  }

  private def onUnsubscribe(repository: String): Unit = {
    Logger.info(s"Unsubscribing to $repository")

    if (clientSubscriptions.isDefinedAt(repository)) {
      val childRef = clientSubscriptions(repository)
      context stop childRef
      clientSubscriptions.remove(repository)

      wsClientRef ! Json.toJson(WsClientMessage(s"Unsubscribed from $repository")).toString()
    } else {
      wsClientRef ! Json.toJson(WsClientError(s"You are note subscribed to $repository")).toString()
    }

  }

  private def onWsClientMessage(rawMessage: String): Any = {
    Logger.info(s"Received message from ws client: $rawMessage")

    val message = try {
      parseRawMessage(rawMessage)
    } catch {
      case e: Exception =>
        Logger.error(s"Error while parsing message: $e", e)
        WsClientError(s"Unexpected message: $rawMessage")
    }

    message match {
      case error: WsClientError =>
        wsClientRef ! Json.toJson(error).toString()
      case _ => self ! message
    }
  }

  private def parseRawMessage(rawMessage: String): Any = {

    val rawJson = Json.parse(rawMessage).as[JsObject]
    val action: Option[String] = rawJson.value.getOrElse("action", JsString("undefined")).asOpt[String]

    val parsedMessage = action match {

      case Some("subscribe") =>
        Json.fromJson[Subscribe](rawJson)
          .getOrElse(WsClientError(s"Invalid subscribe message: ${rawJson.toString()}"))

      case Some("unsubscribe") =>
        Json.fromJson[Unsubscribe](rawJson)
          .getOrElse(WsClientError(s"Invalid unsubscribe message: ${rawJson.toString()}"))

      case _ =>
        WsClientError(s"Unexpected message: ${rawJson.toString()}")
    }

    parsedMessage
  }

  override def preStart(): Unit = Logger.info(s"New WsClientActor started")

  override def postStop(): Unit = Logger.info(s"WsClientActor actor stopped")

}
