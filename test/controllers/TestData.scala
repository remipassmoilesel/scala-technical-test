package controllers

import play.api.libs.json.{JsValue, Json}

import scala.io.Source

object TestData {

  def getCommitsRawResponse: JsValue = {
    val jsonStream = classOf[GithubStatsControllerSpec].getResourceAsStream("/github.stats.topCommitters.rawCommits.json")
    val text = Source.fromInputStream(jsonStream).mkString
    Json.parse(text)
  }

  def getOrderedCommitters: JsValue = {
    val jsonStream = classOf[GithubStatsControllerSpec].getResourceAsStream("/github.stats.topCommitters.response.json")
    val text = Source.fromInputStream(jsonStream).mkString
    Json.parse(text)
  }

}
