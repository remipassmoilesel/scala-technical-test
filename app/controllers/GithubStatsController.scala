package controllers

import githubStats.{GithubComitter, GithubStatsService}
import javax.inject._
import play.api.Logger
import play.api.libs.json.{Json, Writes}
import play.api.mvc._

import scala.concurrent.ExecutionContext


@Singleton
class GithubStatsController @Inject()(cc: ControllerComponents,
                                      githubService: GithubStatsService)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  implicit val committerWrites: Writes[GithubComitter] = Json.writes[GithubComitter]

  def getTopComitters(owner: String, repository: String) = Action.async {
    Logger.debug(s"Asking for top comitters on project: $owner/$repository")
    githubService.getTopComittersOfRepo(owner, repository).map(comitters => {
      Ok(Json.obj("comitters" -> comitters))
    })
  }

}
