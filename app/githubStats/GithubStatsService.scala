package githubStats

import javax.inject.Inject
import play.api.libs.json._
import utils.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GithubStatsService @Inject()(httpClient: HttpClient) {

  def getTopComittersOfRepo(owner: String, repository: String): Future[List[GithubCommitter]] = {
    httpClient.get(GithubApiRoutes.repositoryCommits(owner, repository))
      .map(rawToSortedGithubComitters)
  }

  private def rawToSortedGithubComitters(rawResponse: JsValue): List[GithubCommitter] = {
    EntitiesMapping.jsonToGithubCommitArray(rawResponse.as[JsArray])
      .groupBy(_.authorEmail)
      .map(groupedCommits => {
        val commits = groupedCommits._2
        val firstCommit = commits.head
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
                val totalUsage = groupedLanguages.getOrElse(languageName, List()).foldLeft(0)(_ + _.bytes)
                LanguageUsage(languageName, totalUsage)
              })
              .toStream
              .sortBy(_.bytes)(Ordering[Int].reverse)
              .toList
              .slice(0, 10)
          })
      })
  }

  def getLanguagesOfRepository(githubRepository: GithubRepository): Future[List[LanguageUsage]] = {
    httpClient.get(GithubApiRoutes.languagesOfRepository(githubRepository.owner, githubRepository.name))
      .map(raw => {
        EntitiesMapping.rawToLanguageUsage(raw.as[JsObject])
      })
  }

  def getRepositoriesOfUser(username: String): Future[List[GithubRepository]] = {
    httpClient.get(GithubApiRoutes.repositoriesOfUser(username))
      .map(raw => {
        EntitiesMapping.rawToGithubRepositories(raw.as[JsArray])
      })
  }

}
