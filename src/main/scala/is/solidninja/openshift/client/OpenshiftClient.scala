package is.solidninja
package openshift
package client

import is.solidninja.openshift.api.v1._
import is.solidninja.openshift.client.impl.{HttpOpenshiftCluster, OAuthClusterLogin}
import io.circe._
import io.circe.literal._
import gnieh.diffson.circe._
import gnieh.diffson._
import fs2.{Strategy, Task}
import fs2.async.immutable.Signal
import is.solidninja.k8s.api.v1.ObjectMeta
import org.http4s.{BasicCredentials, Credentials, Uri}
import org.http4s.client.Client
import org.http4s.client.blaze.{BlazeClientConfig, PooledHttp1Client}

import scala.concurrent.ExecutionContext

sealed trait ClusterToken

case class BearerToken(token: String)

case class ProjectId(id: String)

trait OpenshiftCluster {
  def project(id: ProjectId): Task[OpenshiftProject with OpenshiftProjectRaw]
}

trait OpenshiftProject {
  def pod(name: String): Task[Option[Pod]]
  def pods(): Task[Seq[Pod]]

  def deploymentConfig(name: String): Task[Option[DeploymentConfig]]
  def deploymentConfigs(): Task[Seq[DeploymentConfig]]

  def route(name: String): Task[Option[Route]]
  def routes(): Task[Seq[Route]]

  def services(): Task[Seq[Service]]
  def service(name: String): Task[Option[Service]]

  def createDeploymentConfig(dc: DeploymentConfig): Task[DeploymentConfig]
  def createRoute(route: Route): Task[Route]
  def createService(service: Service): Task[Service]

  def patchDeploymentConfig(name: String, patch: JsonPatch): Task[DeploymentConfig]
  def patchRoute(name: String, patch: JsonPatch): Task[Route]
  def patchService(name: String, patch: JsonPatch): Task[Service]
}

// TODO: experimental?
trait OpenshiftProjectRaw {
  def podRaw(name: String): Task[Option[Json]]
  def routeRaw(name: String): Task[Option[Json]]
  def deploymentConfigRaw(name: String): Task[Option[Json]]
  def serviceRaw(name: String): Task[Option[Json]]
}

object OpenshiftCluster {
  import org.http4s.client.blaze._

  def apply(url: Uri, token: Signal[Task, Credentials.Token], insecure: Boolean = false): Task[OpenshiftCluster] = {
    val clientConfig = if (insecure) BlazeClientConfig.insecure else BlazeClientConfig.defaultConfig
    apply(url, token, PooledHttp1Client(config = clientConfig))
  }

  def apply(url: Uri, token: Signal[Task, Credentials.Token], httpClient: Client): Task[OpenshiftCluster] =
    Task.now(new HttpOpenshiftCluster(url, token, httpClient))

}

// FIXME - remove this
object TestApp extends App {

  import ExecutionContext.Implicits.global

  implicit val S: Strategy = Strategy.fromExecutionContext(implicitly[ExecutionContext])

  val url = Uri.uri("https://192.168.42.131:8443")
  val credentials = BasicCredentials("developer", "developer")

  val client = PooledHttp1Client(config = BlazeClientConfig.insecure)

  val testPatch = JsonPatch(
    Add(path = Pointer.root / "spec" / "template" / "spec" / "containers" / "0" / "env",
        value = json"""[{"name":"TEST","value":"-Xfoo"}]""")
  )

  val res = for {
    token <- OAuthClusterLogin.cache(OAuthClusterLogin.basic(client, url, credentials))
    cluster <- OpenshiftCluster(url, token, client)
    project <- cluster.project(ProjectId("myproject"))
    dc <- project.deploymentConfig("dnsmasq")
    patched <- project.createRoute(Route(
      metadata = Some(ObjectMeta(
        name = Some("dnsmasq2")
      )),
      RouteSpec(
        host = "dnsmasq2",
        to = RouteTargetReference(kind = "Service", name = "dnsmasq", weight = 100),
        port = None,
        wildcardPolicy = None
    )))
  } yield (dc, patched)

  println(res.unsafeRun())
}
