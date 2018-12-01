package controllers

import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.specs2.mock.mockito.MockitoMatchers
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

class GithubStatsControllerSpec extends PlaySpec with MockitoSugar with MockitoMatchers {

  private val (app, clientMock) = createMockedApp()

  "GithubStatsController" should {

    "Should return correct result" in {

      when(clientMock.get("https://api.github.com/repos/firecracker-microvm/firecracker/commits?per_page=100")).thenReturn(getCommitsRawResponse)
      val topComitters = route(app, FakeRequest(GET, "/github/statistics/project/firecracker-microvm/firecracker/top-committers")).get

      status(topComitters) mustBe Status.OK
      contentType(topComitters) mustBe Some("application/json")
      contentAsJson(topComitters).equals(getOrderedCommits) mustBe true
    }

  }

  private def getCommitsRawResponse: Future[JsValue] = {
    Future {
      val jsonStream = classOf[GithubStatsControllerSpec].getResourceAsStream("/github.stats.topCommitters.rawCommits.json")
      val text = Source.fromInputStream(jsonStream).mkString
      Json.parse(text)
    }
  }

  private def getOrderedCommits: JsValue = {
    val jsonStream = classOf[GithubStatsControllerSpec].getResourceAsStream("/github.stats.topCommitters.response.json")
    val text = Source.fromInputStream(jsonStream).mkString
    Json.parse(text)
  }

  private def createMockedApp() = {
    val clientMock = mock[HttpClient]

    val app = new GuiceApplicationBuilder()
      .overrides(bind[HttpClient].toInstance(clientMock))
      .build()

    (app, clientMock)
  }

}
