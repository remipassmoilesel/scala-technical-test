package controllers

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.stream.Materializer
import githubStats._
import javax.inject._
import play.api.Logger
import play.api.libs.json.{Json, Writes}
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import services.GithubStatsService
import starsWatch.WsClientActor

import scala.concurrent.ExecutionContext


@Singleton
class GithubStatsController @Inject()(cc: ControllerComponents,
                                      githubStatsService: GithubStatsService)
                                     (implicit exec: ExecutionContext,
                                      system: ActorSystem,
                                      mat: Materializer) extends AbstractController(cc) {

  implicit val committerWrites: Writes[GithubCommitter] = Json.writes[GithubCommitter]
  implicit val languageStatsWrites: Writes[GithubIssueAggregForDay] = Json.writes[GithubIssueAggregForDay]

  def getTopComitters(owner: String, repository: String): Action[AnyContent] = Action.async {

    Logger.debug(s"Asking for top comitters on project: $owner/$repository")

    githubStatsService.getTopComittersOfRepo(GithubRepository(repository, owner)).map(comitters => {
      Ok(Json.obj("comitters" -> comitters))
    })
  }

  def getTopLanguages(username: String): Action[AnyContent] = Action.async {
    implicit val languageStatsWrites: Writes[LanguageUsage] = Json.writes[LanguageUsage]

    Logger.debug(s"Asking for top languages for user: $username")

    githubStatsService.getTopLanguagesOfUser(username).map(languageStats => {
      Ok(Json.obj("languages" -> languageStats))
    })
  }

  def getIssuesOfRepository(owner: String, repository: String, endDateStr: Option[String],
                            daysQueryParam: Option[Int]): Action[AnyContent] = Action.async {

    Logger.debug(s"Asking for issues per day for repository: $repository numberOfDays=$daysQueryParam endDate=$endDateStr")

    val df = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val endDate = if (endDateStr.isDefined) LocalDate.parse(endDateStr.get, df) else LocalDate.now().minusDays(1)
    val days = daysQueryParam.getOrElse(25) // it's a small month, due to Github search API rate limites :(

    githubStatsService.getIssuesPerDayForRepository(GithubRepository(repository, owner), endDate, days).map(issues => {
      Ok(Json.obj("issuesPerDay" -> issues))
    })
  }

  def watchStars: WebSocket = WebSocket.accept[String, String] { request =>

    Logger.debug(s"New websocket connection")

    ActorFlow.actorRef { out =>
      Logger.debug(s"New websocket connection")
      WsClientActor.props(out, githubStatsService)
    }
  }

}
