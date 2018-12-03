import akka.actor.{ActorContext, ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import githubStats.StarWatcherActor.StartWatch
import githubStats._
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import org.specs2.mock.mockito.MockitoMatchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class WsClientActorSpec extends TestKit(ActorSystem("WsClientActorSpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with MockitoSugar
  with MockitoMatchers
  with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "WsClientActorSpec" should {

    "Should warn client if request is incorrect" in {
      val githubStatsService = mock[GithubStatsRepository]
      val mockFactory = mock[StarWatcherActorFactory]

      when(mockFactory.newStarWatcher(any[ActorContext], any[ActorRef]))
        .thenReturn(testActor)

      val wsClient = system.actorOf(WsClientActor.props(testActor, mockFactory))

      wsClient ! "Bad message"
      expectMsg(2.seconds, "{\"message\":\"Unexpected message: Bad message\"}")
    }

  }

}
