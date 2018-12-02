package watchStars

import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger
import play.api.libs.json._
import services.GithubStatsService
import watchStars.WsClientActor.{InvalidMessage, Subscribe, Unsubscribe}

import scala.collection.mutable

object WsClientActor {

  def props(clientRef: ActorRef, githubStatsService: GithubStatsService): Props = Props(new WsClientActor(clientRef, githubStatsService))

  final case class Subscribe(repository: String, intervalSec: Long)

  final case class Unsubscribe(repository: String)

  final case class InvalidMessage(message: String)

  implicit val subscribeReads: Reads[Subscribe] = Json.reads[Subscribe]
  implicit val unsubscribeReads: Reads[Unsubscribe] = Json.reads[Unsubscribe]

}

class WsClientActor(clientRef: ActorRef, githubStatsService: GithubStatsService) extends Actor {

  val clientSubscriptions = mutable.Map[String, ActorRef]()

  override def receive: Receive = {

    case Subscribe(repository, intervalSec) =>
      subscribeToRepository(repository, intervalSec)

    case Unsubscribe(repository) =>
      unsubscribeToRepository(repository)

    case InvalidMessage(message) =>
      clientRef ! message

    case msg: String =>
      parseRawAndSend(msg)

    case arg =>
      throw new Exception(s"Unexpected message: $arg class=${arg.getClass}")

  }

  private def subscribeToRepository(repositoryFullname: String, watchTimeSec: Long): Unit = {
    Logger.info(s"Subscribing to $repositoryFullname with time of $watchTimeSec")
    clientRef ! "subscribed"
  }

  private def unsubscribeToRepository(repositoryFullname: String): Unit = {
    Logger.info(s"Unsubscribing to $repositoryFullname")
    clientRef ! "unsubscribed"
  }

  private def parseRawAndSend(rawMessage: String): Unit = {
    Logger.info(s"Received message from websocket: $rawMessage")

    try {
      val message = parseRawMessage(rawMessage)
      self ! message
    } catch {
      case e: Exception =>
        Logger.error(s"Error while parsing message: $e", e)
        self ! InvalidMessage(s"Unexpected message: $rawMessage")
    }
  }

  private def parseRawMessage(rawMessage: String): Any = {

    val rawJson = Json.parse(rawMessage).as[JsObject]
    val action: Option[String] = rawJson.value.getOrElse("action", JsString("undefined")).asOpt[String]

    val parsedMessage = action match {

      case Some("subscribe") =>
        Json.fromJson[Subscribe](rawJson)
          .getOrElse(InvalidMessage(s"Invalid subscribe message: ${rawJson.toString()}"))

      case Some("unsubscribe") =>
        Json.fromJson[Unsubscribe](rawJson)
          .getOrElse(InvalidMessage(s"Invalid unsubscribe message: ${rawJson.toString()}"))

      case _ =>
        InvalidMessage(s"Unexpected message: ${rawJson.toString()}")
    }

    parsedMessage
  }

  override def preStart(): Unit = Logger.info(s"New WsClientActor started")

  override def postStop(): Unit = Logger.info(s"WsClientActor actor stopped")

}
