package githubStats

import play.api.libs.json.{Json, Writes}

case class GithubCommit(authorName: String, authorEmail: String) {

}

case class GithubCommitter(name: String, email: String, commits: Int) {

}

case class GithubRepository(name: String, owner: String) {

}

object GithubRepository {
  def from(repository: String): GithubRepository = {
    val parts = repository.split("/")
    GithubRepository(parts(0), parts(1))
  }
}

case class LanguageUsage(name: String, bytes: Int) {

}

case class GithubIssue(createdOnDay: String) {

}

case class GithubIssueAggregForDay(date: String, issues: Int, x: Int, y: Int) {

}

case class GithubRepositoryStars(fullName: String, stars: Int) {

}

object GithubRepositoryStars {
  implicit val clientErrorWrites: Writes[GithubRepositoryStars] = Json.writes[GithubRepositoryStars]
}


