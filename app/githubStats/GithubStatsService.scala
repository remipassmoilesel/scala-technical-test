package githubStats

import javax.inject.Inject
import play.api.libs.json.{JsArray, JsString, JsValue}
import utils.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GithubStatsService @Inject()(httpClient: HttpClient) {

  def getTopComittersOfRepo(owner: String, repository: String): Future[List[GithubComitter]] = {
    httpClient.get(getRepoCommitsRoute(owner, repository))
      .map(jsonToSortedGithubComitters)
  }

  private def jsonToSortedGithubComitters(jsValue: JsValue): List[GithubComitter] = {
    val jsArray = jsValue.as[JsArray]

    val committers = jsArray.value
      .map(jsValue => {
        val name = (jsValue \ "commit" \ "author" \ "name").getOrElse(JsString("Unknown name")).as[String]
        val email = (jsValue \ "commit" \ "author" \ "email").getOrElse(JsString("Unknown email")).as[String]
        Map("name" -> name, "email" -> email)
      })
      .groupBy(_.get("email"))
      .map(groupedCommits => {
        val commits = groupedCommits._2
        val firstCommit = commits(0)
        GithubComitter(firstCommit.get("name").get, firstCommit.get("email").get, commits.size)
      })
      .toStream
      .sortBy(_.commits)(Ordering[Int].reverse)
      .toList

    committers
  }

  private def getRepoCommitsRoute(owner: String, repository: String): String = {
    "https://api.github.com/repos/:owner/:repository/commits?per_page=100"
      .replace(":owner", owner)
      .replace(":repository", repository)
  }

}
