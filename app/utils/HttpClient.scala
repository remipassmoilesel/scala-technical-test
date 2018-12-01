package utils

import javax.inject.Inject
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

trait HttpClient {
  def get(url: String, expectedStatus: Int = 200): Future[JsValue]
}

class HttpRequestException(message: String) extends Exception(message) {

}

class HttpClientImpl @Inject()(wsClient: WSClient) extends HttpClient {

  private def checkStatus(response: WSResponse, expectedStatus: Int): Unit = {
    if (response.status != expectedStatus) {
      throw new HttpRequestException(s"Bad status code: ${response.status} ($expectedStatus expected)")
    }
  }

  def get(url: String, expectedStatus: Int = 200): Future[JsValue] = {
    getRequest(url).get().map(response => {
      checkStatus(response, expectedStatus)
      response.json
    })
  }

  private def getRequest(url: String): WSRequest = {
    wsClient.url(url)
      .withRequestTimeout(10000.millis)
  }

}
