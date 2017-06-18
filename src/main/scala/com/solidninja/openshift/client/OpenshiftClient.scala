package com.solidninja.openshift.client

import fs2.Task
import io.circe._
import cats.syntax.either._
import org.http4s._
import org.http4s.client.Client
import org.http4s.headers.Authorization

sealed trait ClusterToken
case class BearerToken(token: String)

case class ProjectId(id: String)

trait OpenshiftCluster {

  def project(id: ProjectId): Task[OpenshiftProject]

}

object OpenshiftCluster {
  import org.http4s.client.blaze._

  // TODO: how to implement login support

  def apply(url: Uri, mkToken: => Credentials.Token, insecure: Boolean = false): Task[OpenshiftCluster] = {
    val clientConfig = if (insecure) BlazeClientConfig.insecure else BlazeClientConfig.defaultConfig
    apply(url, mkToken, PooledHttp1Client(config = clientConfig))
  }

  def apply(url: Uri, mkToken: => Credentials.Token, httpClient: Client): Task[OpenshiftCluster] = {
    val client = new OpenshiftClient(httpClient, url, mkToken)
    Task.now {
      new OpenshiftCluster {
        override def project(id: ProjectId): Task[OpenshiftProject] = Task.now(OpenshiftProject(client, id))
      }
    }
  }

}

trait OpenshiftProject {

  def pods(): Task[Seq[Pod]]

}

private[client] class OpenshiftClient(client: Client, url: Uri, mkToken: => Credentials.Token) {

  import Decoders._
  import org.http4s.circe._

  private val v1api = url / "api" / "v1"

  def listPods(projectId: ProjectId): Task[Seq[Pod]] =
    get[PodList](v1api / "namespaces" / projectId.id / "pods").map(_.items)

  // FIXME: handle unauthorized requests in a more principled fashion - perhaps a Task[Credentials.Token]?
  private def get[T](uri: Uri)(implicit D: Decoder[T]): Task[T] =
    client.expect(
      Request(method = Method.GET, uri = uri, headers = Headers(Authorization(mkToken)))
    )(jsonOf[T])
}

object OpenshiftProject {

  def apply(client: OpenshiftClient, projectId: ProjectId) = new OpenshiftProject {
    override def pods(): Task[Seq[Pod]] = client.listPods(projectId)
  }

}

case class Pod(name: String)
case class PodList(items: List[Pod])

object Decoders {
  implicit val decodePodList: Decoder[PodList] = Decoder.instance(c =>
    for {
      items <- c.downField("items").as[List[Pod]]
    } yield PodList(items))

  implicit val decodePod: Decoder[Pod] = new Decoder[Pod] {
    final def apply(c: HCursor): Decoder.Result[Pod] =
      for {
        name <- c.downField("metadata").downField("name").as[String]
      } yield {
        Pod(name)
      }
  }
}

object TestApp extends App {

  val url = Uri.uri("https://172.22.22.60:8443")
  val token = BearerToken("C6wogF3DF2w3s4zANY5gqUbZXArR0sPsLhyyys3kZgs")
  def mkToken = Credentials.Token(AuthScheme.Bearer, token.token)

  val res = for {
    cluster <- OpenshiftCluster(url, mkToken, insecure = true)
    project <- cluster.project(ProjectId("myproject"))
    pods <- project.pods()
  } yield pods

  println(res.unsafeRun())
}
