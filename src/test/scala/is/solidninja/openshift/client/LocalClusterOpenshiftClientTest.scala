package is.solidninja
package openshift
package client

import java.nio.file.{Files, Paths}

import io.circe._
import cats.effect._
import cats.implicits._
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
      io {
        withLocalCluster { cluster =>
          val app = "flask-hello-world"
          def create(proj: OpenshiftProject, item: v1.EitherTopLevel): IO[Unit] = item match {
            case Left(dc: v1.DeploymentConfig) => proj.createDeploymentConfig(dc).map(_ => ())
            case Left(route: v1.Route) => proj.createRoute(route).map(_ => ())
            case Right(svc: k8sv1.Service) => proj.createService(svc).map(_ => ())
            case other => throw new RuntimeException(s"BUG: not expecting to create object like $other")
          }

          for {
            proj <- cluster.project(ProjectId("myproject"))
            list <- readItemList(app)
            _ <- list.items.traverse(create(proj, _))
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
  implicit val httpClient = PooledHttp1Client[IO](config = BlazeClientConfig.insecure)

  def withLocalCluster(test: (OpenshiftCluster) => IO[Unit]): IO[Unit] =
    for {
      token <- OAuthClusterLogin.cache(
        OAuthClusterLogin.basic(httpClient, LocalCluster.Config.uri, LocalCluster.Config.credentials))
      cluster <- OpenshiftCluster(LocalCluster.Config.uri, token, httpClient)
      _ <- test(cluster)
    } yield ()

  // FIXME - this has a lot in common with the supporting code of TemplateTest
  def readItemList(name: String): IO[v1.TemplateList] =
    for {
      uri <- IO(getClass.getResource(s"/template/$name/expanded.json")).map(_.toURI)
      bytes <- IO(Files.readAllBytes(Paths.get(uri)))
      parsed <- parser.parse(new String(bytes)).fold(IO.raiseError, IO.pure) // FIXME common pattern with no method?
      list <- parsed.as[v1.TemplateList].fold(IO.raiseError, IO.pure)
    } yield list

}
