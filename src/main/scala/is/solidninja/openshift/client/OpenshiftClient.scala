package is.solidninja
package openshift
package client

import fs2.Task
import fs2.async.immutable.Signal

import io.circe._

import gnieh.diffson.circe._

import org.http4s.{Credentials, Uri}
import org.http4s.client.Client

import is.solidninja.openshift.api.v1._
import is.solidninja.openshift.client.impl.HttpOpenshiftCluster

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

  def apply(url: Uri, token: Signal[Task, Credentials.Token], httpClient: Client): Task[OpenshiftCluster] =
    Task.now(new HttpOpenshiftCluster(url, token, httpClient))

}
