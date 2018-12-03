package githubStats

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import javax.inject.Inject
import play.api.libs.json._
import utils.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GithubStatsRepository @Inject()(httpClient: HttpClient) {

  def getTopComittersOfRepo(githubRepository: GithubRepo): Future[List[GithubCommitter]] = {
    getLastCommitsOfRepository(githubRepository)
      .map(commits => {
        commits.groupBy(_.authorEmail)
          .map(groupedCommits => {
            val commits = groupedCommits._2
            val firstCommit = commits.head
            GithubCommitter(firstCommit.authorName, firstCommit.authorEmail, commits.size)
          })
          .toStream
          .sortBy(_.commits)(Ordering[Int].reverse)
          .toList
          .slice(0, 10)
      })

  }

  private def getLastCommitsOfRepository(githubRepository: GithubRepo): Future[List[GithubCommit]] = {
    httpClient.get(GithubApiRoutes.repositoryCommits(githubRepository))
      .map(rawResponse => GithubEntitiesMapper.jsonToGithubCommitArray(rawResponse.as[JsArray]))
  }

  def getTopLanguagesOfUser(username: String): Future[List[GithubLanguageUsage]] = {
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
                GithubLanguageUsage(languageName, totalUsage)
              })
              .toStream
              .sortBy(_.bytes)(Ordering[Int].reverse)
              .toList
              .slice(0, 10)
          })
      })
  }

  private def getLanguagesOfRepository(githubRepository: GithubRepo): Future[List[GithubLanguageUsage]] = {
    httpClient.get(GithubApiRoutes.languagesOfRepository(githubRepository))
      .map(raw => GithubEntitiesMapper.rawToLanguageUsage(raw.as[JsObject]))
  }

  private def getRepositoriesOfUser(username: String): Future[List[GithubRepo]] = {
    httpClient.get(GithubApiRoutes.repositoriesOfUser(username))
      .map(raw => GithubEntitiesMapper.rawToGithubRepositories(raw.as[JsArray]))
  }

  def getIssuesPerDayForRepository(githubRepository: GithubRepo,
                                   endDate: LocalDate,
                                   periodDays: Int): Future[List[GithubIssueAggregForDay]] = {

    val formatter = DateTimeFormatter.ofPattern("dd/MM")
    val startDate = endDate.minusDays(periodDays - 1)

    val issuesPerDay = Future.sequence(
      Stream.range(0, periodDays)
        .map(startDate.plusDays(_))
        .map(getIssuesForDay(githubRepository, _))
        .toList
    )

    issuesPerDay.map(allDaysIssues => {
      allDaysIssues
        .zipWithIndex
        .map {
          case (oneDayIssues, index) =>
            val issues = oneDayIssues.size
            val shortDate: String = startDate.plusDays(index).format(formatter)
            GithubIssueAggregForDay(shortDate, issues, index, issues)
        }
    })
  }

  private def getIssuesForDay(githubRepository: GithubRepo, day: LocalDate): Future[List[GithubIssue]] = {
    val formattedDay = day.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    httpClient.get(GithubApiRoutes.searchIssuesCreatedOnDay(githubRepository, formattedDay))
      .map(raw => GithubEntitiesMapper.rawSearchToGithubIssues(raw.as[JsObject]))
  }

  def getStarNumberOfRepo(githubRepository: GithubRepo): Future[GithubRepoStars] = {
    httpClient.get(GithubApiRoutes.repository(githubRepository))
      .map(raw => GithubEntitiesMapper.rawToGithubRepositoryStars(raw.as[JsObject]))
  }

}
