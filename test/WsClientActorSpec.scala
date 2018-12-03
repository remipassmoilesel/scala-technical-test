import akka.actor.{ActorContext, ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import githubStats.StarWatcherActor.StartWatch
import githubStats.WsClientActor.WsClientMessage
import githubStats._
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import org.specs2.mock.mockito.MockitoMatchers

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

      val testChild = TestActorRef(StarWatcherActor.props(testActor, githubStatsService))
      val mockFactory = mock[StarWatcherActorFactory]
      when(mockFactory.newStarWatcher(any[ActorContext], any[ActorRef])).thenReturn(testChild)

      val wsClient = TestActorRef(WsClientActor.props(testActor, mockFactory))

      wsClient ! "Bad message"
      expectMsg(2.seconds, "{\"message\":\"Unexpected message: Bad message\"}")
    }

    "Should register client on subscribe request" in {
      val githubStatsService = mock[GithubStatsRepository]

      val testChild = TestActorRef(StarWatcherActor.props(testActor, githubStatsService))
      val mockFactory = mock[StarWatcherActorFactory]
      when(mockFactory.newStarWatcher(any[ActorContext], any[ActorRef])).thenReturn(testChild)

      val wsClient = TestActorRef(WsClientActor.props(testActor, mockFactory))

      wsClient ! "{\"action\": \"subscribe\",     \"repository\": \"kubernetes/kubernetes\", \"intervalSec\": 3}"
      expectMsg(2.seconds, "{\"message\":\"Subscribed to kubernetes/kubernetes with interval of 3 seconds\"}")
    }

    "Should not register client if already subscribed" in {
      val githubStatsService = mock[GithubStatsRepository]

      val testChild = TestActorRef(StarWatcherActor.props(testActor, githubStatsService))
      val mockFactory = mock[StarWatcherActorFactory]
      when(mockFactory.newStarWatcher(any[ActorContext], any[ActorRef])).thenReturn(testChild)

      val wsClient = TestActorRef(WsClientActor.props(testActor, mockFactory))

      wsClient ! "{\"action\": \"subscribe\",     \"repository\": \"kubernetes/kubernetes\", \"intervalSec\": 3}"
      expectMsg(2.seconds, "{\"message\":\"Subscribed to kubernetes/kubernetes with interval of 3 seconds\"}")

      wsClient ! "{\"action\": \"subscribe\",     \"repository\": \"kubernetes/kubernetes\", \"intervalSec\": 3}"
      expectMsg(2.seconds, "{\"message\":\"You are already subscribed to kubernetes/kubernetes\"}")

    }

    "Should unregister client on unsubscribe request" in {
      val githubStatsService = mock[GithubStatsRepository]

      val testChild = TestActorRef(StarWatcherActor.props(testActor, githubStatsService))
      val mockFactory = mock[StarWatcherActorFactory]
      when(mockFactory.newStarWatcher(any[ActorContext], any[ActorRef])).thenReturn(testChild)

      val wsClient = TestActorRef(WsClientActor.props(testActor, mockFactory))

      wsClient ! "{\"action\": \"subscribe\",     \"repository\": \"kubernetes/kubernetes\", \"intervalSec\": 3}"
      expectMsg(2.seconds, "{\"message\":\"Subscribed to kubernetes/kubernetes with interval of 3 seconds\"}")

      wsClient ! "{\"action\": \"unsubscribe\",     \"repository\": \"kubernetes/kubernetes\", \"intervalSec\": 3}"
      expectMsg(2.seconds, "{\"message\":\"Unsubscribed from kubernetes/kubernetes\"}")
    }

    "Should warn user client on bad unsubscribe request" in {
      val githubStatsService = mock[GithubStatsRepository]

      val testChild = TestActorRef(StarWatcherActor.props(testActor, githubStatsService))
      val mockFactory = mock[StarWatcherActorFactory]
      when(mockFactory.newStarWatcher(any[ActorContext], any[ActorRef])).thenReturn(testChild)

      val wsClient = TestActorRef(WsClientActor.props(testActor, mockFactory))

      wsClient ! "{\"action\": \"unsubscribe\",     \"repository\": \"kubernetes/kubernetes\", \"intervalSec\": 3}"
      expectMsg(2.seconds, "{\"message\":\"You are note subscribed to kubernetes/kubernetes\"}")
    }

  }

}
