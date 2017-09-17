package is.solidninja
package openshift
package client

import cats.effect.IO
import fs2.async.immutable.Signal
import gnieh.diffson.circe._
import org.http4s.{Credentials, Uri}
import org.http4s.client.Client
import is.solidninja.openshift.api.v1._
import is.solidninja.openshift.client.impl.HttpOpenshiftCluster

sealed trait ClusterToken

case class BearerToken(token: String)

case class ProjectId(id: String)

trait OpenshiftCluster {
  def project(id: ProjectId): IO[OpenshiftProject]
}

trait OpenshiftProject {
  def pod(name: String): IO[Option[Pod]]
  def pods(): IO[Seq[Pod]]

  def deploymentConfig(name: String): IO[Option[DeploymentConfig]]
  def deploymentConfigs(): IO[Seq[DeploymentConfig]]

  def route(name: String): IO[Option[Route]]
  def routes(): IO[Seq[Route]]

  def services(): IO[Seq[Service]]
  def service(name: String): IO[Option[Service]]

  def replicationControllers(): IO[Seq[ReplicationController]]
  def replicationController(name: String): IO[Option[ReplicationController]]

  def createDeploymentConfig(dc: DeploymentConfig): IO[DeploymentConfig]
  def createRoute(route: Route): IO[Route]
  def createService(service: Service): IO[Service]

  def patchDeploymentConfig(name: String, patch: JsonPatch): IO[DeploymentConfig]
  def patchRoute(name: String, patch: JsonPatch): IO[Route]
  def patchService(name: String, patch: JsonPatch): IO[Service]
}

object OpenshiftCluster {

  def apply(url: Uri, token: Signal[IO, Credentials.Token], httpClient: Client[IO]): IO[OpenshiftCluster] =
    IO.pure(new HttpOpenshiftCluster(url, token, httpClient))

}
