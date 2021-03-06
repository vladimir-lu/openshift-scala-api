package is.solidninja
package openshift
package client
package impl

import is.solidninja.k8s.api.v1.{PodList, ServiceList, ReplicationControllerList}
import is.solidninja.openshift.api.v1.{Service => v1Service, _}
import fs2.{Strategy, Task}
import fs2.async.immutable.Signal
import io.circe._
import io.circe.syntax._
import gnieh.diffson.circe._
import gnieh.diffson.circe.DiffsonProtocol._
import org.http4s._
import org.http4s.client._
import org.http4s.headers.{Authorization, Location}

private[client] class HttpOpenshiftCluster(url: Uri, token: Signal[Task, Credentials.Token], httpClient: Client)
    extends OpenshiftCluster {
  val client = new HttpOpenshiftClient(httpClient, url, token)

  override def project(id: ProjectId): Task[OpenshiftProject] =
    Task.now(new HttpOpenshiftProject(client, id))
}

private[client] class HttpOpenshiftProject(client: HttpOpenshiftClient, projectId: ProjectId)
    extends OpenshiftProject {
  override def pods(): Task[Seq[Pod]] = client.listPods(projectId)

  override def pod(name: String): Task[Option[Pod]] = client.getPod(projectId, name)

  override def deploymentConfigs(): Task[Seq[DeploymentConfig]] = client.listDeploymentConfigs(projectId)

  override def deploymentConfig(name: String): Task[Option[DeploymentConfig]] =
    client.getDeploymentConfig(projectId, name)

  override def routes(): Task[Seq[Route]] = client.listRoutes(projectId)

  override def route(name: String): Task[Option[Route]] = client.getRoute(projectId, name)

  override def services(): Task[Seq[v1Service]] = client.listServices(projectId)

  override def service(name: String): Task[Option[v1Service]] = client.getService(projectId, name)

  override def replicationControllers(): Task[Seq[ReplicationController]] = client.getReplicationControllers(projectId)

  override def replicationController(name: String): Task[Option[ReplicationController]] =
    client.getReplicationController(projectId, name)

  override def patchDeploymentConfig(name: String, patch: JsonPatch): Task[DeploymentConfig] =
    client.patchDeploymentConfig(projectId, name, patch)

  override def patchRoute(name: String, patch: JsonPatch): Task[Route] =
    client.patchRoute(projectId, name, patch)

  override def patchService(name: String, patch: JsonPatch): Task[v1Service] =
    client.patchService(projectId, name, patch)

  override def createDeploymentConfig(dc: DeploymentConfig): Task[DeploymentConfig] =
    client.createDeploymentConfig(projectId, dc)

  override def createRoute(route: Route): Task[Route] =
    client.createRoute(projectId, route)

  override def createService(service: v1Service): Task[v1Service] =
    client.createService(projectId, service)
}

private[client] class HttpOpenshiftClient(client: Client, url: Uri, token: Signal[Task, Credentials.Token]) {

  import JsonProtocol._
  import org.http4s.circe._

  private val v1k8s = url / "api" / "v1"
  private val v1oapi = url / "oapi" / "v1"

  private def namespacek8s(projectId: ProjectId) = v1k8s / "namespaces" / projectId.id
  private def namespace(projectId: ProjectId) = v1oapi / "namespaces" / projectId.id

  def listPods(projectId: ProjectId): Task[Seq[Pod]] =
    get[PodList](namespacek8s(projectId) / "pods").map(_.items)

  def getPod(projectId: ProjectId, name: String): Task[Option[Pod]] =
    getOpt[Pod](namespacek8s(projectId) / "pods" / name)

  def listServices(projectId: ProjectId): Task[Seq[v1Service]] =
    get[ServiceList](namespacek8s(projectId) / "services").map(_.items)

  def getService(projectId: ProjectId, name: String): Task[Option[v1Service]] =
    getOpt[v1Service](namespacek8s(projectId) / "services" / name)

  def listRoutes(projectId: ProjectId): Task[Seq[Route]] =
    get[RouteList](namespace(projectId) / "routes").map(_.items)

  def getRoute(projectId: ProjectId, name: String): Task[Option[Route]] =
    getOpt[Route](namespace(projectId) / "routes" / name)

  def listDeploymentConfigs(projectId: ProjectId): Task[Seq[DeploymentConfig]] =
    get[DeploymentConfigList](namespace(projectId) / "deploymentconfigs").map(_.items)

  def getDeploymentConfig(projectId: ProjectId, name: String): Task[Option[DeploymentConfig]] =
    getOpt[DeploymentConfig](namespace(projectId) / "deploymentconfigs" / name)

  def getReplicationControllers(projectId: ProjectId): Task[Seq[ReplicationController]] =
    get[ReplicationControllerList](namespacek8s(projectId) / "replicationcontrollers").map(_.items)

  def getReplicationController(projectId: ProjectId, name: String): Task[Option[ReplicationController]] =
    getOpt[ReplicationController](namespacek8s(projectId) / "replicationcontrollers" / name)

  def patchDeploymentConfig(projectId: ProjectId, name: String, thePatch: JsonPatch): Task[DeploymentConfig] =
    patch[DeploymentConfig](namespace(projectId) / "deploymentconfigs" / name, thePatch.asJson)

  def patchService(projectId: ProjectId, name: String, thePatch: JsonPatch): Task[v1Service] =
    patch[v1Service](namespacek8s(projectId) / "services" / name, thePatch.asJson)

  def patchRoute(projectId: ProjectId, name: String, thePatch: JsonPatch): Task[Route] =
    patch[Route](namespace(projectId) / "routes" / name, thePatch.asJson)

  def createDeploymentConfig(projectId: ProjectId, dc: DeploymentConfig): Task[DeploymentConfig] =
    post[DeploymentConfig](namespace(projectId) / "deploymentconfigs", dc)

  def createRoute(projectId: ProjectId, route: Route): Task[Route] =
    post[Route](namespace(projectId) / "routes", route)

  def createService(projectId: ProjectId, service: v1Service): Task[v1Service] =
    post[v1Service](namespacek8s(projectId) / "services", service)

  private def getOpt[T: Decoder](uri: Uri): Task[Option[T]] =
    get[T](uri).map(Option(_)).handle {
      case UnexpectedStatus(Status.NotFound) => None
    }

  private def get[T: Decoder](uri: Uri): Task[T] = req[T](Request(method = Method.GET, uri = uri))

  private def patch[T: Decoder](uri: Uri, patch: Json): Task[T] =
    req[T](
      Request(method = Method.PATCH, uri = uri)
        .withBody(patch)
        // override the content-type header
        .map(_.putHeaders(Header("Content-Type", "application/json-patch+json")))
    )

  private def post[T: Decoder: Encoder](uri: Uri, obj: T): Task[T] =
    req[T](
      Request(method = Method.POST, uri = uri)
        .withBody(obj)(jsonEncoderOf[T])
    )

  private def req[T: Decoder](reqT: Request): Task[T] = req[T](Task.now(reqT))

  // FIXME: handle unauthorized requests in a more principled fashion - perhaps a Task[Credentials.Token]?
  private def req[T: Decoder](reqT: Task[Request]): Task[T] =
    for {
      tok <- token.get
      req <- reqT.map(_.putHeaders(Authorization(tok)))
      resp <- client.expect(req)(jsonOf[T])
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
