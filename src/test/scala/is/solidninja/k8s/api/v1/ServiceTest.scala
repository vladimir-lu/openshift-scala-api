package is.solidninja
package k8s
package api
package v1

import org.scalatest.{FreeSpec, Matchers}

import io.circe._
import io.circe.literal._
import io.circe.syntax._

import JsonProtocol._

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

      val serviceSpec = ServiceSpec(
        `type` = "ClusterIP",
        sessionAffinity = Some("None"),
        selector = Some(Selector(Map("deploymentconfig" -> Json.fromString("dnsmasq")))),
        ports = Some(
          List(
            ServicePort("53-tcp", Port(53), "TCP", Port(53)),
            ServicePort("53-udp", Port(53), "UDP", Port(53))
          ))
      )

      val expected = Service(
        metadata = Some(meta),
        spec = serviceSpec
      )

      j.as[Service] should equal(Right(expected))
//      j.as[Service].toTry.get.asJson should equal (j)
    }

    "should encode a simple service" in {
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
            )))
      )

      val service = Service(
        metadata = Some(meta),
        spec = ServiceSpec(
          `type` = "ClusterIP"
        )
      )

      service.asJson should equal(json"""{
        "kind" : "Service",
        "apiVersion" : "v1",
        "metadata" : {
          "name" : "dnsmasq",
          "namespace" : null,
          "labels" : {
            "app" : "dnsmasq"
          },
          "annotations" : {
            "openshift.io/generated-by" : "OpenShiftWebConsole"
          },
          "uid" : null,
          "resourceVersion" : null,
          "creationTimestamp" : null,
          "selfLink" : null
        },
        "spec" : {
          "type" : "ClusterIP",
          "clusterIP" : null,
          "externalIPs" : null,
          "externalName" : null,
          "loadBalancerIP" : null,
          "ports" : null,
          "selector" : null,
          "sessionAffinity" : null
        }
      }""")
    }
  }

}
