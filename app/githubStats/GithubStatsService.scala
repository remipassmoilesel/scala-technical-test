package githubStats

import javax.inject.Inject
import play.api.libs.json._
import utils.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// TODO: move entity mapping to a dedicated object

class GithubStatsService @Inject()(httpClient: HttpClient) {

  def getTopComittersOfRepo(owner: String, repository: String): Future[List[GithubCommitter]] = {
    httpClient.get(GithubApiRoutes.getRepositoryCommitsRoute(owner, repository))
      .map(rawToSortedGithubComitters)
  }

  private def rawToSortedGithubComitters(jsValue: JsValue): List[GithubCommitter] = {
    jsValue.as[JsArray].value
      .map(value => {
        val name = (value \ "commit" \ "author" \ "name").getOrElse(JsString("Unknown name")).as[String]
        val email = (value \ "commit" \ "author" \ "email").getOrElse(JsString("Unknown email")).as[String]
        GithubCommit(name, email)
      })
      .groupBy(_.authorEmail)
      .map(groupedCommits => {
        val commits = groupedCommits._2
        val firstCommit = commits(0)
        GithubCommitter(firstCommit.authorName, firstCommit.authorEmail, commits.size)
      })
      .toStream
      .sortBy(_.commits)(Ordering[Int].reverse)
      .toList
      .slice(0, 10)
  }

  def getTopLanguagesOfUser(username: String): Future[List[LanguageUsage]] = {
    getRepositoriesOfUser(username)
      .flatMap(repositories => {
        Future.sequence(repositories.map(getLanguagesOfRepository))
          .map(allLangUsagesByRepo => {
            allLangUsagesByRepo.flatten.groupBy(_.name)
          })
          .map(groupedLanguages => {
            groupedLanguages.keys
              .map(languageName => {
                val totalUsage = groupedLanguages.getOrElse(languageName, List()).foldLeft(0)(_ + _.lines)
                LanguageUsage(languageName, totalUsage)
              })
              .toStream
              .sortBy(_.lines)(Ordering[Int].reverse)
              .toList
              .slice(0, 10)
          })
      })
  }

  def getLanguagesOfRepository(githubRepository: GithubRepository): Future[List[LanguageUsage]] = {
    httpClient.get(GithubApiRoutes.getLanguagesOfRepositoryRoute(githubRepository.owner, githubRepository.name))
      .map(rawToLanguageUsage)
  }

  private def rawToLanguageUsage(jsValue: JsValue): List[LanguageUsage] = {
    val obj = jsValue.as[JsObject]
    obj.keys
      .map(key => {
        LanguageUsage(key, obj.value.get(key).get.asInstanceOf[JsNumber].value.toInt)
      })
      .toList
  }

  def getRepositoriesOfUser(username: String): Future[List[GithubRepository]] = {
    httpClient.get(GithubApiRoutes.getRepositoryOfUserRoute(username))
      .map(rawToGithubRepositories(_, username))
  }

  private def rawToGithubRepositories(jsValue: JsValue, owner: String): List[GithubRepository] = {
    jsValue.as[JsArray].value
      .map(value => {
        val name = (value \ "name").getOrElse(JsString("Unknown name")).as[String]
        GithubRepository(name, owner)
      })
      .toList
  }

}
