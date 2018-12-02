package controllers

import play.api.libs.json.{JsValue, Json}

import scala.io.Source

object TestData {

  def getRawCommits: JsValue = {
    getJsonFile("/topCommitters/rawCommits.json")
  }

  def getTopComitters: JsValue = {
    getJsonFile("/topCommitters/topComitters.json")
  }

  private def getJsonFile(resourcePath: String): JsValue = {
    val jsonStream = getClass.getResourceAsStream(resourcePath)
    val text = Source.fromInputStream(jsonStream).mkString
    Json.parse(text)
  }

}
