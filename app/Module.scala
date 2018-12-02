
import com.google.inject.AbstractModule
import utils.{HttpClient, HttpClientImpl}

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[HttpClient]).to(classOf[HttpClientImpl])
  }

}
