package controllers

import githubStats.{GithubCommitter, GithubStatsService, LanguageUsage}
import javax.inject._
import play.api.Logger
import play.api.libs.json.{Json, Writes}
import play.api.mvc._

import scala.concurrent.ExecutionContext


@Singleton
class GithubStatsController @Inject()(cc: ControllerComponents,
                                      githubService: GithubStatsService)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def getTopComitters(owner: String, repository: String) = Action.async {
    implicit val committerWrites: Writes[GithubCommitter] = Json.writes[GithubCommitter]

    Logger.debug(s"Asking for top comitters on project: $owner/$repository")
    githubService.getTopComittersOfRepo(owner, repository).map(comitters => {
      Ok(Json.obj("comitters" -> comitters))
    })
  }

  def getTopLanguages(username: String) = Action.async {
    implicit val languageStatsWrites: Writes[LanguageUsage] = Json.writes[LanguageUsage]

    Logger.debug(s"Asking for top languages for user: $username")
    githubService.getTopLanguagesOfUser(username).map(languageStats => {
      Ok(Json.obj("languages" -> languageStats))
    })
  }

}
