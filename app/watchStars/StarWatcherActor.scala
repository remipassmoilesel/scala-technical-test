package watchStars

import akka.actor.{Actor, ActorRef, Props, Timers}
import githubStats.GithubRepository
import play.api.Logger
import play.api.libs.json.Json
import services.GithubStatsService
import watchStars.StarWatcherActor.{CheckStars, StartWatch}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object StarWatcherActor {

  def props(clientRef: ActorRef, githubStatsService: GithubStatsService): Props =
    Props(new StarWatcherActor(clientRef, githubStatsService))

  final case class StartWatch(repository: String, watchTimeSec: Long)

  final case class CheckStars(repository: String)

}

class StarWatcherActor(clientRef: ActorRef, githubStatsService: GithubStatsService) extends Actor with Timers {

  override def receive: Receive = {

    case StartWatch(repository, intervalSec) =>
      onStartWatch(repository, intervalSec)

    case CheckStars(repository) =>
      onCheckStars(repository)

    case arg =>
      throw new Exception(s"Unexpected message: $arg class=${arg.getClass}")

  }

  def onStartWatch(repository: String, intervalSec: Long): Unit = {
    Logger.info(s"Start watching stars of $repository interval=$intervalSec")
    timers.startPeriodicTimer(repository, CheckStars(repository), intervalSec.second)
  }

  def onCheckStars(repository: String): Unit = {
    Logger.info(s"Checking stars of $repository interval=$repository")

    githubStatsService.getStarNumberOfRepo(GithubRepository.from(repository))
      .map(stars => clientRef ! Json.toJson(stars).toString())
  }

  override def preStart(): Unit = Logger.info(s"StarWatcherActor started")

  override def postStop(): Unit = Logger.info(s"StarWatcherActor stopped")

}
