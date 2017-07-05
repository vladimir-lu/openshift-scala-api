package com.solidninja.openshift.client.impl

import com.solidninja.k8s.api.v1.{PodList, ServiceList}
import com.solidninja.openshift.api.v1._
import com.solidninja.openshift.client._
import fs2.{Strategy, Task}
import fs2.async.immutable.Signal
import io.circe.{Decoder, Json}
import org.http4s.{Service => HService, _}
import org.http4s.client._
import org.http4s.headers.{Authorization, Location}

private[client] class HttpOpenshiftCluster(url: Uri, token: Signal[Task, Credentials.Token], httpClient: Client)
    extends OpenshiftCluster {
  val client = new HttpOpenshiftClient(httpClient, url, token)

  override def project(id: ProjectId): Task[OpenshiftProject with OpenshiftProjectRaw] =
    Task.now(new HttpOpenshiftProject(client, id))
}

private[client] class HttpOpenshiftProject(client: HttpOpenshiftClient, projectId: ProjectId)
    extends OpenshiftProject
    with OpenshiftProjectRaw {
  override def pods(): Task[Seq[Pod]] = client.listPods(projectId)

  override def pod(name: String): Task[Option[Pod]] = client.getPod(projectId, name)

  override def deploymentConfigs(): Task[Seq[DeploymentConfig]] = client.listDeploymentConfigs(projectId)

  override def deploymentConfig(name: String): Task[Option[DeploymentConfig]] =
    client.getDeploymentConfig(projectId, name)

  override def routes(): Task[Seq[Route]] = client.listRoutes(projectId)

  override def route(name: String): Task[Option[Route]] = client.getRoute(projectId, name)

  override def services(): Task[Seq[Service]] = client.listServices(projectId)

  override def service(name: String): Task[Option[Service]] = client.getService(projectId, name)

  override def podRaw(name: String): Task[Option[Json]] = client.getPodRaw(projectId, name)

  override def routeRaw(name: String): Task[Option[Json]] = client.getRouteRaw(projectId, name)

  override def deploymentConfigRaw(name: String): Task[Option[Json]] = client.getDeploymentConfigRaw(projectId, name)

  override def serviceRaw(name: String): Task[Option[Json]] = client.getServiceRaw(projectId, name)
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

  def getPod(projectId: ProjectId, name: String): Task[Option[Pod]] =
    getOpt[Pod](namespacek8s(projectId) / "pods" / name)

  def getPodRaw(projectId: ProjectId, name: String): Task[Option[Json]] =
    getOpt[Json](namespacek8s(projectId) / "pods" / name)

  def listServices(projectId: ProjectId): Task[Seq[Service]] =
    get[ServiceList](namespacek8s(projectId) / "services").map(_.items)

  def getService(projectId: ProjectId, name: String): Task[Option[Service]] =
    getOpt[Service](namespacek8s(projectId) / "services" / name)

  def getServiceRaw(projectId: ProjectId, name: String): Task[Option[Json]] =
    getOpt[Json](namespacek8s(projectId) / "services" / name)

  def listRoutes(projectId: ProjectId): Task[Seq[Route]] =
    get[RouteList](namespace(projectId) / "routes").map(_.items)

  def getRoute(projectId: ProjectId, name: String): Task[Option[Route]] =
    getOpt[Route](namespace(projectId) / "routes" / name)

  def getRouteRaw(projectId: ProjectId, name: String): Task[Option[Json]] =
    getOpt[Json](namespace(projectId) / "routes" / name)

  def listDeploymentConfigs(projectId: ProjectId): Task[Seq[DeploymentConfig]] =
    get[DeploymentConfigList](namespace(projectId) / "deploymentconfigs").map(_.items)

  def getDeploymentConfig(projectId: ProjectId, name: String): Task[Option[DeploymentConfig]] =
    getOpt[DeploymentConfig](namespace(projectId) / "deploymentconfigs" / name)

  def getDeploymentConfigRaw(projectId: ProjectId, name: String): Task[Option[Json]] =
    getOpt[Json](namespace(projectId) / "deploymentconfigs" / name)

  private def getOpt[T: Decoder](uri: Uri): Task[Option[T]] =
    get[T](uri).map(Option(_)).handle {
      case UnexpectedStatus(Status.NotFound) => None
    }

  // FIXME: handle unauthorized requests in a more principled fashion - perhaps a Task[Credentials.Token]?
  private def get[T: Decoder](uri: Uri): Task[T] =
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
