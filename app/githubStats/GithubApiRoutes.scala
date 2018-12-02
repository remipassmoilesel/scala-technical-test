package githubStats

object GithubApiRoutes {

  val baseUri = "https://api.github.com"

  def repositoryCommits(repository: GithubRepository): String = {
    s"$baseUri/repos/${repository.owner}/${repository.name}/topCommitters?per_page=100"
  }

  def repositoriesOfUser(username: String): String = {
    s"$baseUri/users/$username/repos?per_page=100"
  }

  def languagesOfRepository(repository: GithubRepository): String = {
    s"$baseUri/repos/${repository.owner}/${repository.name}/languages"
  }

  def searchIssuesCreatedOnDay(repository: GithubRepository, date: String): String = {
    s"$baseUri/search/issues?q=repo:${repository.owner}/${repository.name}+created:$date&per_page=100"
  }

  def repository(repository: GithubRepository): String = {
    s"$baseUri/repos/${repository.owner}/${repository.name}"
  }

}
