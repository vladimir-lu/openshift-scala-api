package com.solidninja.openshift.client

import fs2.{Strategy, Task}
import com.solidninja.openshift.api.v1._
import com.solidninja.openshift.client.impl.{HttpOpenshiftCluster, OAuthClusterLogin}
import fs2.async.immutable.Signal
import org.http4s.client.Client
import org.http4s.client.blaze.{BlazeClientConfig, PooledHttp1Client}
import org.http4s.{Service => HService, _}

import scala.concurrent.ExecutionContext

sealed trait ClusterToken

case class BearerToken(token: String)

case class ProjectId(id: String)

trait OpenshiftCluster {
  def project(id: ProjectId): Task[OpenshiftProject]
}

trait OpenshiftProject {
  def pods(): Task[Seq[Pod]]
  def deploymentConfigs(): Task[Seq[DeploymentConfig]]
  def routes(): Task[Seq[Route]]
  def services(): Task[Seq[Service]]
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

object TestApp extends App {

  import ExecutionContext.Implicits.global

  implicit val S: Strategy = Strategy.fromExecutionContext(implicitly[ExecutionContext])

  val url = Uri.uri("https://192.168.42.131:8443")
  val credentials = BasicCredentials("developer", "developer")

  val client = PooledHttp1Client(config = BlazeClientConfig.insecure)

  val res = for {
    token <- OAuthClusterLogin.cache(OAuthClusterLogin.basic(client, url, credentials))
    cluster <- OpenshiftCluster(url, token, client)
    project <- cluster.project(ProjectId("myproject"))
    dcs <- project.services()
    pods <- project.pods()
  } yield (dcs, pods)

  println(res.unsafeRun())
}
