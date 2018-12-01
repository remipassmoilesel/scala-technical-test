package githubStats

case class GithubCommit(authorName: String, authorEmail: String) {

}

case class GithubCommitter(name: String, email: String, commits: Int) {

}

case class GithubRepository(name: String, owner: String) {

}

case class LanguageUsage(name: String, bytes: Int) {

}
