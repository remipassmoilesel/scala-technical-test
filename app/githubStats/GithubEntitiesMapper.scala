package githubStats

import play.api.libs.json._

object GithubEntitiesMapper {

  def jsonToGithubCommitArray(raw: JsArray): List[GithubCommit] = {
    raw.value
      .map(value => {
        val name = (value \ "commit" \ "author" \ "name").getOrElse(JsString("Unknown name")).as[String]
        val email = (value \ "commit" \ "author" \ "email").getOrElse(JsString("Unknown email")).as[String]
        GithubCommit(name, email)
      })
      .toList
  }

  def rawToLanguageUsage(raw: JsObject): List[LanguageUsage] = {
    raw.keys
      .map(key => {
        LanguageUsage(key, raw.value(key).asInstanceOf[JsNumber].value.toInt)
      })
      .toList
  }

  def rawToGithubRepositories(raw: JsArray): List[GithubRepository] = {
    raw.value
      .map(value => {
        val name = (value \ "name").getOrElse(JsString("Unknown name")).as[String]
        val owner = (value \ "owner" \ "login").getOrElse(JsString("Unknown owner")).as[String]
        GithubRepository(name, owner)
      })
      .toList
  }

  def rawSearchToGithubIssues(raw: JsObject): scala.List[GithubIssue] = {
    val searchResult = (raw \ "items").getOrElse(JsArray()).as[JsArray]
    searchResult.value
      .map(value => {
        val createdAt = (value \ "created_at").getOrElse(JsString("Unknown date")).as[String].substring(0, 10)
        GithubIssue(createdAt)
      })
      .toList
  }

  def rawToGithubRepositoryStars(raw: JsObject): GithubRepositoryStars = {
    val starCount = (raw \ "stargazers_count").getOrElse(JsNumber(-1)).as[Int]
    val fullName = (raw \ "full_name").getOrElse(JsString("Unknown full name")).as[String]

    GithubRepositoryStars(fullName, starCount)
  }

}
