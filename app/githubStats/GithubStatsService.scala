package githubStats

import javax.inject.Inject
import utils.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GithubStatsService @Inject()(httpClient: HttpClient) {

  def getTopComittersForProject(projectId: String): Future[List[GithubComitter]] = {
    Future {
      List(GithubComitter("jean", "jean@claude.fr", 24))
    }
  }

}
