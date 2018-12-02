package starsWatch

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import githubStats.{GithubRepository, GithubRepositoryStars}
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import org.specs2.mock.mockito.MockitoMatchers
import services.GithubStatsService
import starsWatch.StarWatcherActor.StartWatch

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class StarWatcherActorSpec extends TestKit(ActorSystem("StarWatcherActorSpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with MockitoSugar
  with MockitoMatchers
  with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "StarWatcherActorSpec" should {

    "Should start watching stars" in {
      val githubStatsService = mock[GithubStatsService]
      val starWatcher = system.actorOf(StarWatcherActor.props(testActor, githubStatsService))

      when(githubStatsService.getStarNumberOfRepo(any[GithubRepository]))
        .thenReturn(Future {
          GithubRepositoryStars("test-owner/test-repository", 3)
        })

      starWatcher ! StartWatch("test-owner/test-repository", 1)
      expectMsg(2.seconds, "{\"fullName\":\"test-owner/test-repository\",\"stars\":3}")
    }

  }

}
