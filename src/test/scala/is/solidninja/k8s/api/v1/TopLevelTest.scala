package is.solidninja
package k8s
package api
package v1

import io.circe._
import io.circe.literal._
import org.scalatest.{FreeSpec, Matchers}

import Decoders._

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
                env = Some(Nil)
              ))
          )
        ),
        Service(
          metadata = None,
          spec = ServiceSpec()
        )
      )

      j.hcursor.downField("items").as[List[TopLevel]] should equal(Right(expected))
    }
  }

}
