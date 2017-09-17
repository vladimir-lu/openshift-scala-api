package is
package solidninja
package openshift
package client

import java.nio.file.{Files, Paths}

import io.circe._

import fs2._

import org.http4s.client.blaze.{BlazeClientConfig, PooledHttp1Client}
import org.scalatest.{FreeSpec, Matchers}

import is.solidninja.openshift.client.impl.OAuthClusterLogin
import is.solidninja.openshift.api.v1
import is.solidninja.k8s.api.{v1 => k8sv1}
import is.solidninja.scalatest._

class LocalClusterOpenshiftClientTest extends FreeSpec with Matchers with Fs2Spec {

  import LocalClusterOpenshiftClientTest._

  "A client for a local OpenShift cluster" - {

    // FIXME - this test fails if app already exists - delete needs implementing
    "should be able to create a 'hello world' http service and bring it up" taggedAs LocalCluster in {
      task {
        withLocalCluster { cluster =>
          val app = "flask-hello-world"
          def create(proj: OpenshiftProject)(item: v1.EitherTopLevel): Task[Unit] = item match {
            case Left(dc: v1.DeploymentConfig) => proj.createDeploymentConfig(dc).map(_ => ())
            case Left(route: v1.Route) => proj.createRoute(route).map(_ => ())
            case Right(svc: k8sv1.Service) => proj.createService(svc).map(_ => ())
            case other => throw new RuntimeException(s"BUG: not expecting to create object like $other")
          }

          for {
            proj <- cluster.project(ProjectId("myproject"))
            list <- readItemList(app)
            _ <- Task.traverse(list.items)(create(proj))
            dc <- proj.deploymentConfig(app)
            route <- proj.route(app)
            svc <- proj.service(app)
          } yield {
            dc.flatMap(_.name) should contain(app)
            route.flatMap(_.name) should contain(app)
            val _ = svc.flatMap(_.name) should contain(app)
          }
        }
      }
    }
  }
}

object LocalClusterOpenshiftClientTest {
  import v1.JsonProtocol._

  implicit val ec = scala.concurrent.ExecutionContext.global
  implicit val S = Strategy.fromExecutionContext(ec)
  implicit val httpClient = PooledHttp1Client(config = BlazeClientConfig.insecure)

  def withLocalCluster(test: (OpenshiftCluster) => Task[Unit]): Task[Unit] =
    for {
      token <- OAuthClusterLogin.cache(
        OAuthClusterLogin.basic(httpClient, LocalCluster.Config.uri, LocalCluster.Config.credentials))
      cluster <- OpenshiftCluster(LocalCluster.Config.uri, token, httpClient)
      _ <- test(cluster)
    } yield ()

  // FIXME - this has a lot in common with the supporting code of TemplateTest
  def readItemList(name: String): Task[v1.TemplateList] =
    for {
      uri <- Task(getClass.getResource(s"/template/$name/expanded.json")).map(_.toURI)
      bytes <- Task(Files.readAllBytes(Paths.get(uri)))
      parsed <- Task.fromAttempt(parser.parse(new String(bytes)))
      list <- Task.fromAttempt(parsed.as[v1.TemplateList])
    } yield list

}
