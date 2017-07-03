package com.solidninja.openshift.client

import fs2.Task
import com.solidninja.openshift.api.v1._
import com.solidninja.openshift.client.impl.HttpOpenshiftCluster
import org.http4s.client.Client
import org.http4s.{Service => HService, _}

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

  // TODO: how to implement login support

  def apply(url: Uri, mkToken: => Credentials.Token, insecure: Boolean = false): Task[OpenshiftCluster] = {
    val clientConfig = if (insecure) BlazeClientConfig.insecure else BlazeClientConfig.defaultConfig
    apply(url, mkToken, PooledHttp1Client(config = clientConfig))
  }

  def apply(url: Uri, mkToken: => Credentials.Token, httpClient: Client): Task[OpenshiftCluster] =
    Task.now(new HttpOpenshiftCluster(url, mkToken, httpClient))

}

object TestApp extends App {

  val url = Uri.uri("https://192.168.42.131:8443")
  val token = BearerToken("ghT_FZreqS11otg3xixmYugqENe-2ra1lH3wmf0crYE")
  def mkToken = Credentials.Token(AuthScheme.Bearer, token.token)

  val res = for {
    cluster <- OpenshiftCluster(url, mkToken, insecure = true)
    project <- cluster.project(ProjectId("myproject"))
    dcs <- project.services()
  } yield dcs

  println(res.unsafeRun())
}
