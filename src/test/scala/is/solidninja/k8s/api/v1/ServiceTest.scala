package is.solidninja
package k8s
package api
package v1

import org.scalatest.{FreeSpec, Matchers}
import io.circe._
import io.circe.literal._

import Decoders._

class ServiceTest extends FreeSpec with Matchers {

  "Service v1" - {
    "should decode from a simple example" in {
      val j: Json = json"""{
        "kind": "Service",
        "apiVersion": "v1",
        "metadata": {
            "name": "dnsmasq",
            "creationTimestamp": null,
            "labels": {
                "app": "dnsmasq"
            },
            "annotations": {
                "openshift.io/generated-by": "OpenShiftWebConsole"
            }
        },
        "spec": {
            "ports": [
                {
                    "name": "53-tcp",
                    "protocol": "TCP",
                    "port": 53,
                    "targetPort": 53
                },
                {
                    "name": "53-udp",
                    "protocol": "UDP",
                    "port": 53,
                    "targetPort": 53
                }
            ],
            "selector": {
                "deploymentconfig": "dnsmasq"
            },
            "type": "ClusterIP",
            "sessionAffinity": "None"
        },
        "status": {
            "loadBalancer": {}
        }}"""

      // FIXME - how to get rid of copy-pasted material?

      val meta = ObjectMeta(
        name = Some("dnsmasq"),
        labels = Some(
          Map(
            "app" -> "dnsmasq"
          )),
        annotations = Some(
          Annotations(
            Map(
              "openshift.io/generated-by" -> Json.fromString("OpenShiftWebConsole")
            ))),
        namespace = None,
        uid = None,
        resourceVersion = None,
        creationTimestamp = None,
        selfLink = None
      )

      val serviceSpec = ServiceSpec()

      val expected = Service(
        metadata = Some(meta),
        spec = serviceSpec
      )

      j.as[Service] should equal(Right(expected))
    }
  }

}
