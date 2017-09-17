package is.solidninja
package openshift
package client

import cats.effect._
import gnieh.diffson._
import gnieh.diffson.circe._
import io.circe.literal._
import is.solidninja.k8s.api.v1.ObjectMeta
import is.solidninja.openshift.api.v1._
import is.solidninja.openshift.client.impl.OAuthClusterLogin
import org.http4s.client.blaze.{BlazeClientConfig, PooledHttp1Client}
import org.http4s.{BasicCredentials, Uri}

// FIXME - remove this manual app from the test project and write some integration tests against minishift
object OpenshiftClientManualApp extends App {

  val url = Uri.uri("https://localhost:8443")
  val credentials = BasicCredentials("developer", "developer")

  val client = PooledHttp1Client[IO](config = BlazeClientConfig.insecure)

  val testPatch = JsonPatch(
    Add(path = Pointer.root / "spec" / "template" / "spec" / "containers" / "0" / "env",
        value = json"""[{"name":"TEST","value":"-Xfoo"}]""")
  )

  val res = for {
    token <- OAuthClusterLogin.cache(OAuthClusterLogin.basic(client, url, credentials))
    cluster <- OpenshiftCluster(url, token, client)
    project <- cluster.project(ProjectId("myproject"))
    dc <- project.deploymentConfig("dnsmasq")
    patched <- project.createRoute(
      Route(
        metadata = Some(
          ObjectMeta(
            name = Some("dnsmasq2")
          )),
        RouteSpec(
          host = "dnsmasq2",
          to = RouteTargetReference(kind = "Service", name = "dnsmasq", weight = Some(100)),
          port = None,
          wildcardPolicy = None
        )
      ))
  } yield (dc, patched)

  println(res.unsafeRunSync())
}
