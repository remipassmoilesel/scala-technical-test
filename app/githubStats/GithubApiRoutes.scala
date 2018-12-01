package githubStats

object GithubApiRoutes {

  def repositoryCommits(owner: String, repository: String): String = {
    s"https://api.github.com/repos/$owner/$repository/commits?per_page=100"
  }

  def repositoriesOfUser(username: String): String = {
    s"https://api.github.com/users/$username/repos?per_page=100"
  }

  def languagesOfRepository(owner: String, repository: String): String = {
    s"https://api.github.com/repos/$owner/$repository/languages"
  }

}
