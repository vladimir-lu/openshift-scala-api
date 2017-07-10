package is.solidninja
package k8s
package api
package v1

import org.scalatest.{FreeSpec, Matchers}

import io.circe._
import io.circe.literal._

import JsonProtocol._

class TopLevelTest extends FreeSpec with Matchers {

  "TopLevel objects" - {
    "should decode from a list of pods and services" in {

      val j: Json =
        json"""{
        "items": [
          {
            "kind": "Pod",
            "metadata": null,
            "spec": {
              "volumes": [],
              "containers": [
                {
                  "name": "deployment",
                  "image": "openshift/origin-deployer:v1.5.1",
                  "env": [],
                  "resources": {},
                  "volumeMounts": [],
                  "terminationMessagePath": "/dev/termination-log",
                  "imagePullPolicy": "IfNotPresent"
                }
              ]
            },
            "status": {}
        },
        {
          "kind": "Service",
          "apiVersion": "v1",
          "metadata": null,
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
          }
        }
      ]}"""

      val expected: List[TopLevel] = List(
        Pod(
          metadata = None,
          spec = PodSpec(
            volumes = Some(Nil),
            containers = List(
              Container(
                image = ImageName("openshift/origin-deployer:v1.5.1"),
                imagePullPolicy = "IfNotPresent",
                args = None,
                command = None,
                env = Some(Nil),
                name = Some("deployment"),
                resources = Some(ResourceRequirements()),
                terminationMessagePath = Some("/dev/termination-log"),
                volumeMounts = Some(Nil)
              )
            )
          )
        ),
        Service(
          metadata = None,
          spec = ServiceSpec(
            `type` = "ClusterIP",
            sessionAffinity = Some("None"),
            selector = Some(Selector(Map("deploymentconfig" -> Json.fromString("dnsmasq")))),
            ports = Some(
              List(
                ServicePort("53-tcp", Port(53), "TCP", Port(53)),
                ServicePort("53-udp", Port(53), "UDP", Port(53))
              ))
          )
        )
      )

      j.hcursor.downField("items").as[List[TopLevel]] should equal(Right(expected))
      // FIXME - add encoding test back to json
    }
  }

}
