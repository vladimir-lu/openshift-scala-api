package com.solidninja.openshift.client.impl

import com.solidninja.k8s.api.v1.{PodList, ServiceList}
import com.solidninja.openshift.api.v1._
import com.solidninja.openshift.client._
import fs2.{Strategy, Task}
import fs2.async.immutable.Signal
import io.circe.Decoder
import org.http4s.{Service => HService, _}
import org.http4s.client._
import org.http4s.headers.{Authorization, Location}

private[client] class HttpOpenshiftCluster(url: Uri, token: Signal[Task, Credentials.Token], httpClient: Client)
    extends OpenshiftCluster {
  val client = new HttpOpenshiftClient(httpClient, url, token)

  override def project(id: ProjectId): Task[OpenshiftProject] = Task.now(new HttpOpenshiftProject(client, id))
}

private[client] class HttpOpenshiftProject(client: HttpOpenshiftClient, projectId: ProjectId)
    extends OpenshiftProject {
  override def pods(): Task[Seq[Pod]] = client.listPods(projectId)

  override def deploymentConfigs(): Task[Seq[DeploymentConfig]] = client.listDeploymentConfigs(projectId)

  override def routes(): Task[Seq[Route]] = client.listRoutes(projectId)

  override def services(): Task[Seq[Service]] = client.listServices(projectId)
}

private[client] class HttpOpenshiftClient(client: Client, url: Uri, token: Signal[Task, Credentials.Token]) {

  import com.solidninja.openshift.api.v1.Decoders._
  import org.http4s.circe._

  private val v1k8s = url / "api" / "v1"
  private val v1oapi = url / "oapi" / "v1"

  private def namespacek8s(projectId: ProjectId) = v1k8s / "namespaces" / projectId.id
  private def namespace(projectId: ProjectId) = v1oapi / "namespaces" / projectId.id

  def listPods(projectId: ProjectId): Task[Seq[Pod]] =
    get[PodList](namespacek8s(projectId) / "pods").map(_.items)

  def listServices(projectId: ProjectId): Task[Seq[Service]] =
    get[ServiceList](namespacek8s(projectId) / "services").map(_.items)

  def listRoutes(projectId: ProjectId): Task[Seq[Route]] =
    get[RouteList](namespace(projectId) / "routes").map(_.items)

  def listDeploymentConfigs(projectId: ProjectId): Task[Seq[DeploymentConfig]] =
    get[DeploymentConfigList](namespace(projectId) / "deploymentconfigs").map(_.items)

  // FIXME: handle unauthorized requests in a more principled fashion - perhaps a Task[Credentials.Token]?
  private def get[T](uri: Uri)(implicit D: Decoder[T]): Task[T] =
    for {
      tok <- token.get
      resp <- client.expect(
        Request(
          method = Method.GET,
          uri = uri,
          headers = Headers(Authorization(tok))
        ))(jsonOf[T])
    } yield resp
}

object OAuthClusterLogin {

  def cache(t: Task[Credentials.Token]): Task[Signal[Task, Credentials.Token]] =
    for {
      tok <- t
    } yield fs2.async.mutable.Signal.constant[Task, Credentials.Token](tok)

  def basic(client: Client, url: Uri, credentials: BasicCredentials)(implicit S: Strategy): Task[Credentials.Token] = {
    val req = Request(
      method = Method.GET,
      uri = url / "oauth" / "authorize"
        +? ("response_type", "token")
        +? ("client_id", "openshift-challenging-client"),
      headers = Headers(
        Header("X-CSRF-Token", "1"),
        Authorization(credentials)
      )
    )

    client.fetch[Credentials.Token](req)(getToken)
  }

  // FIXME: Define own exception types

  private[client] def getToken(resp: Response)(implicit S: Strategy): Task[Credentials.Token] = Task {
    resp.headers
      .collectFirst {
        case Location(v) => v.uri.fragment.flatMap(extractToken)
      }
      .flatten
      .getOrElse(throw new RuntimeException("Unable to get location header and token"))
  }

  private[client] def extractToken(fragment: String): Option[Credentials.Token] = {
    val fragmentMap = fragment
      .split("&")
      .toList
      .map { kv =>
        kv.split("=").toList match {
          case k :: v :: Nil => (k, v)
          // FIXME - ugly style
          case x => throw new RuntimeException(s"Expected key=value, got $x")
        }
      }
      .toMap

    if (fragmentMap.get("token_type").contains("Bearer"))
      fragmentMap.get("access_token").map(v => Credentials.Token(AuthScheme.Bearer, v))
    else None
  }
}
