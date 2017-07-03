package com.solidninja.openshift.client.impl

import com.solidninja.k8s.api.v1.{PodList, ServiceList}
import com.solidninja.openshift.api.v1._
import com.solidninja.openshift.client._
import fs2.Task
import io.circe.Decoder
import org.http4s.{Service => HService, _}
import org.http4s.client.Client
import org.http4s.headers.Authorization

private[client] class HttpOpenshiftCluster(url: Uri, mkToken: => Credentials.Token, httpClient: Client)
    extends OpenshiftCluster {
  val client = new HttpOpenshiftClient(httpClient, url, mkToken)

  override def project(id: ProjectId): Task[OpenshiftProject] = Task.now(new HttpOpenshiftProject(client, id))
}

private[client] class HttpOpenshiftProject(client: HttpOpenshiftClient, projectId: ProjectId)
    extends OpenshiftProject {
  override def pods(): Task[Seq[Pod]] = client.listPods(projectId)

  override def deploymentConfigs(): Task[Seq[DeploymentConfig]] = client.listDeploymentConfigs(projectId)

  override def routes(): Task[Seq[Route]] = client.listRoutes(projectId)

  override def services(): Task[Seq[Service]] = client.listServices(projectId)
}

private[client] class HttpOpenshiftClient(client: Client, url: Uri, mkToken: => Credentials.Token) {

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
    client.expect(
      Request(method = Method.GET, uri = uri, headers = Headers(Authorization(mkToken)))
    )(jsonOf[T])
}
