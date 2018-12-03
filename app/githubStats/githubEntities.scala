package githubStats

import play.api.libs.json.{Json, Writes}

object GithubApiRoutes {

  val baseUri = "https://api.github.com"

  def repositoryCommits(repository: GithubRepo): String = {
    s"$baseUri/repos/${repository.owner}/${repository.name}/commits?per_page=100"
  }

  def repositoriesOfUser(username: String): String = {
    s"$baseUri/users/$username/repos?per_page=100"
  }

  def languagesOfRepository(repository: GithubRepo): String = {
    s"$baseUri/repos/${repository.owner}/${repository.name}/languages"
  }

  def searchIssuesCreatedOnDay(repository: GithubRepo, date: String): String = {
    s"$baseUri/search/issues?q=repo:${repository.owner}/${repository.name}+created:$date&per_page=100"
  }

  def repository(repository: GithubRepo): String = {
    s"$baseUri/repos/${repository.owner}/${repository.name}"
  }

}

case class GithubCommit(authorName: String, authorEmail: String) {

}

case class GithubCommitter(name: String, email: String, commits: Int) {

}

case class GithubLanguageUsage(name: String, bytes: Int) {

}

case class GithubIssue(createdOnDay: String) {

}

case class GithubIssueAggregForDay(date: String, issues: Int, x: Int, y: Int) {

}

case class GithubRepoStars(fullName: String, stars: Int) {

}

object GithubRepoStars {
  implicit val clientErrorWrites: Writes[GithubRepoStars] = Json.writes[GithubRepoStars]
}

case class GithubRepo(name: String, owner: String) {

}

object GithubRepo {
  def from(repository: String): GithubRepo = {
    val parts = repository.split("/")
    GithubRepo(parts(0), parts(1))
  }
}
