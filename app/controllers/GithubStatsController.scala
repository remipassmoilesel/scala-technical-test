package controllers

import java.time.LocalDateTime

import githubStats._
import javax.inject._
import play.api.Logger
import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import services.GithubStatsService

import scala.concurrent.ExecutionContext


@Singleton
class GithubStatsController @Inject()(cc: ControllerComponents,
                                      githubService: GithubStatsService)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def getTopComitters(owner: String, repository: String): Action[AnyContent] = Action.async {
    implicit val committerWrites: Writes[GithubCommitter] = Json.writes[GithubCommitter]

    Logger.debug(s"Asking for top comitters on project: $owner/$repository")

    githubService.getTopComittersOfRepo(GithubRepository(owner, repository)).map(comitters => {
      Ok(Json.obj("comitters" -> comitters))
    })
  }

  def getTopLanguages(username: String): Action[AnyContent] = Action.async {
    implicit val languageStatsWrites: Writes[LanguageUsage] = Json.writes[LanguageUsage]

    Logger.debug(s"Asking for top languages for user: $username")

    githubService.getTopLanguagesOfUser(username).map(languageStats => {
      Ok(Json.obj("languages" -> languageStats))
    })
  }

  def getIssuesForLastMonth(owner: String, repository: String): Action[AnyContent] = Action.async {
    implicit val languageStatsWrites: Writes[GithubIssueAggregForDay] = Json.writes[GithubIssueAggregForDay]

    Logger.debug(s"Asking for issues per day for repository: $repository")

    val endDate = LocalDateTime.now().minusDays(1)
    val days = 25 // it's a small month
    githubService.getIssuesPerDayForRepository(GithubRepository(owner, repository), endDate, days).map(issues => {
      Ok(Json.obj("issuesPerDay" -> issues))
    })
  }

}
