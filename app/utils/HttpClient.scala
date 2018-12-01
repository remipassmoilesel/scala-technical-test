package utils

import com.google.inject.ImplementedBy
import javax.inject.Inject
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSClient, WSRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

@ImplementedBy(classOf[HttpClientImpl])
trait HttpClient {
  def get(url: String): Future[JsValue]
}

class HttpClientImpl @Inject()(wsClient: WSClient) extends HttpClient {

  def get(url: String): Future[JsValue] = {
    getRequest(url).get().map(response => response.json)
  }

  private def getRequest(url: String): WSRequest = {
    wsClient.url(url)
      .withRequestTimeout(10000.millis)
  }

}
