package githubStats

object GithubApiRoutes {

  def getRepositoryCommitsRoute(owner: String, repository: String): String = {
    s"https://api.github.com/repos/$owner/$repository/commits?per_page=100"
  }

  def getRepositoryOfUserRoute(username: String): String = {
    s"https://api.github.com/users/$username/repos?per_page=100"
  }

  def getLanguagesOfRepositoryRoute(owner: String, repository: String): String = {
    s"https://api.github.com/repos/$owner/$repository/languages"
  }

}
