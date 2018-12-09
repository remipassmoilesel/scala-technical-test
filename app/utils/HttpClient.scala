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

  def get(url: String, expectedStatus: Int = 200): Future[JsValue] = {
    getRequest(url).get().map(response => {
      checkStatus(response, expectedStatus, url)
      response.json
    })
  }

  private def getRequest(url: String): WSRequest = {
    val request = wsClient.url(url)
      .withRequestTimeout(10000.millis)

    sys.env.get("AUTHORIZATION_HEADER").map { auth =>
      request.withHttpHeaders(("Authorization", sys.env("AUTHORIZATION_HEADER")))
    }.getOrElse(request)
  }

  private def checkStatus(response: WSResponse, expectedStatus: Int, requestUrl: String): Unit = {
    if (response.status != expectedStatus) {
      throw new HttpRequestException(
        s"""
        Bad status code: ${response.status} ${response.statusText} ($expectedStatus expected)

        Request=${requestUrl}

        ResponseBody=${response.json}

        ResponseHeaders=${response.headers}
        """)
    }
  }

}
