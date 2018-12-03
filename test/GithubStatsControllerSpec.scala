import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.specs2.mock.mockito.MockitoMatchers
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GithubStatsControllerSpec extends PlaySpec with MockitoSugar with MockitoMatchers {

  private val (app, clientMock) = createMockedApp()

  "GithubStatsController" should {

    "Should return list of top committers of specified project" in {
      when(clientMock.get("https://api.github.com/repos/firecracker-microvm/firecracker/topCommitters?per_page=100"))
        .thenReturn(Future {
          TestData.getJsonFile("/topCommitters/rawCommits.json")
        })

      val topComitters = route(app, FakeRequest(GET, "/github/statistics/project/firecracker-microvm/firecracker/top-committers")).get

      status(topComitters) mustBe Status.OK
      contentType(topComitters) mustBe Some("application/json")
      contentAsJson(topComitters) mustEqual TestData.getJsonFile("/topCommitters/topComitters.json")
    }

    "Should return list of top language of specified user" in {

      when(clientMock.get("https://api.github.com/users/KouglofKabyle/repos?per_page=100"))
        .thenReturn(Future {
          TestData.getJsonFile("/topLanguages/repoList.json")
        })

      when(clientMock.get("https://api.github.com/repos/KouglofKabyle/amorcePAI/languages"))
        .thenReturn(Future {
          TestData.getJsonFile("/topLanguages/amorcePAI.json")
        })

      when(clientMock.get("https://api.github.com/repos/KouglofKabyle/ngDraggable/languages"))
        .thenReturn(Future {
          TestData.getJsonFile("/topLanguages/ngDraggable.json")
        })

      when(clientMock.get("https://api.github.com/repos/KouglofKabyle/projet-gl-2015/languages"))
        .thenReturn(Future {
          TestData.getJsonFile("/topLanguages/projet-gl-2015.json")
        })

      when(clientMock.get("https://api.github.com/repos/KouglofKabyle/ui-jar/languages"))
        .thenReturn(Future {
          TestData.getJsonFile("/topLanguages/ui-jar.json")
        })

      val topLanguages = route(app, FakeRequest(GET, "/github/statistics/user/KouglofKabyle/top-languages")).get

      status(topLanguages) mustBe Status.OK
      contentType(topLanguages) mustBe Some("application/json")
      contentAsJson(topLanguages) mustEqual TestData.getJsonFile("/topLanguages/topLanguages.json")
    }

    "Should return number of issue per day" in {

      when(clientMock.get("https://api.github.com/search/issues?q=repo:kubernetes/kubernetes+created:2018-10-07&per_page=100"))
        .thenReturn(Future {
          TestData.getJsonFile("/issuesPerDay/07.json")
        })

      when(clientMock.get("https://api.github.com/search/issues?q=repo:kubernetes/kubernetes+created:2018-10-08&per_page=100"))
        .thenReturn(Future {
          TestData.getJsonFile("/issuesPerDay/08.json")
        })

      when(clientMock.get("https://api.github.com/search/issues?q=repo:kubernetes/kubernetes+created:2018-10-09&per_page=100"))
        .thenReturn(Future {
          TestData.getJsonFile("/issuesPerDay/09.json")
        })

      val topLanguages = route(app, FakeRequest(GET, "/github/statistics/project/kubernetes/kubernetes/issues?endDate=2018-10-09&numberOfDays=3")).get

      status(topLanguages) mustBe Status.OK
      contentType(topLanguages) mustBe Some("application/json")
      contentAsJson(topLanguages) mustEqual TestData.getJsonFile("/issuesPerDay/issuesPerDay.json")
    }

  }

  private def createMockedApp() = {
    val clientMock = mock[HttpClient]

    val app = new GuiceApplicationBuilder()
      .overrides(bind[HttpClient].toInstance(clientMock))
      .build()

    (app, clientMock)
  }

}
