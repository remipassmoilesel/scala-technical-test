package githubStats

object GithubApiRoutes {

  def repositoryCommits(repository: GithubRepository): String = {
    s"https://api.github.com/repos/${repository.owner}/${repository.name}/commits?per_page=100"
  }

  def repositoriesOfUser(username: String): String = {
    s"https://api.github.com/users/$username/repos?per_page=100"
  }

  def languagesOfRepository(repository: GithubRepository): String = {
    s"https://api.github.com/repos/${repository.owner}/${repository.name}/languages"
  }

  def searchIssuesCreatedOnDay(repository: GithubRepository, date: String): String = {
    s"https://api.github.com/search/issues?q=repo:${repository.owner}/${repository.name}+created:$date&per_page=100"
  }

}
