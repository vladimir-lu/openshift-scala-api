package com.solidninja.openshift.api.v1

import Decoders._
import com.solidninja.k8s.api.v1._

import io.circe._
import io.circe.literal._

import org.scalatest.{FreeSpec, Matchers}

class RouteTest extends FreeSpec with Matchers {

  "Route v1" - {
    "should decode based on a simple example" in {
      val j: Json = json"""{
          "kind": "Route",
          "apiVersion": "v1",
          "metadata": {
              "name": "dnsmasq",
              "creationTimestamp": null,
              "annotations": {
                  "openshift.io/host.generated": "true"
              }
          },
          "spec": {
              "host": "dnsmasq-myproject.192.168.42.131.nip.io",
              "to": {
                  "kind": "Service",
                  "name": "dnsmasq",
                  "weight": 100
              },
              "port": {
                  "targetPort": "53-tcp"
              },
              "wildcardPolicy": "None"
          },
          "status": {
              "ingress": [
                  {
                      "host": "dnsmasq-myproject.192.168.42.131.nip.io",
                      "routerName": "router",
                      "conditions": [
                          {
                              "type": "Admitted",
                              "status": "True",
                              "lastTransitionTime": "2017-07-03T11:36:29Z"
                          }
                      ],
                      "wildcardPolicy": "None"
                  }
              ]
          }
      }"""


      val expected = Route(
        metadata = Some(ObjectMeta.empty.copy(
          name = Some("dnsmasq"),
          annotations = Some(Annotations(Map(
            "openshift.io/host.generated" -> Json.fromString("true")
          ))
        ))),
        spec = RouteSpec(
          host = "dnsmasq-myproject.192.168.42.131.nip.io",
          to = RouteTargetReference(
            kind = "Service",
            name = "dnsmasq"
          )
        )
      )

      j.as[Route] should equal(Right(expected))
    }
  }

}
