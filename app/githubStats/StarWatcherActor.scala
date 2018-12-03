package githubStats

import akka.actor.{Actor, ActorContext, ActorRef, Props, Timers}
import githubStats.StarWatcherActor.{CheckStars, StartWatch}
import play.api.Logger
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object StarWatcherActor {

  def props(wsClientRef: ActorRef, githubStatsRepo: GithubStatsRepository): Props =
    Props(new StarWatcherActor(wsClientRef, githubStatsRepo))

  final case class StartWatch(repository: String, watchTimeSec: Long)

  final case class CheckStars(repository: String)

}

class StarWatcherActor(wsClientRef: ActorRef, githubStatsRepo: GithubStatsRepository) extends Actor with Timers {

  override def receive: Receive = {

    case StartWatch(repository, intervalSec) =>
      onStartWatch(repository, intervalSec)

    case CheckStars(repository) =>
      onCheckStars(repository)

  }

  def onStartWatch(repository: String, intervalSec: Long): Unit = {
    Logger.info(s"Start watching stars of $repository interval=$intervalSec")
    timers.startPeriodicTimer(repository, CheckStars(repository), intervalSec.second)
  }

  def onCheckStars(repository: String): Unit = {
    Logger.info(s"Checking stars of $repository interval=$repository")

    githubStatsRepo.getStarNumberOfRepo(GithubRepo.from(repository))
      .map(stars => wsClientRef ! Json.toJson(stars).toString())
  }

  override def preStart(): Unit = Logger.info(s"StarWatcherActor started")

  override def postStop(): Unit = Logger.info(s"StarWatcherActor stopped")

}

class StarWatcherActorFactory(githubStatsRepo: GithubStatsRepository) {

  def newStarWatcher(context: ActorContext, wsClientRef: ActorRef): ActorRef = {
    context.actorOf(StarWatcherActor.props(wsClientRef, githubStatsRepo))
  }

}
