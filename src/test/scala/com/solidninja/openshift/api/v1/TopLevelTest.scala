package com.solidninja.openshift.api.v1

import io.circe._
import io.circe.literal._
import org.scalatest.{FreeSpec, Matchers}
import com.solidninja.k8s.api.v1._
import Decoders._

class TopLevelTest extends FreeSpec with Matchers {

  "Oapi TopLevel" - {
    "should decode from a list of routes and deploymentconfigs" in {
      val j: Json = json"""{
      "items": [
        {
          "kind": "Route",
          "apiVersion": "v1",
          "metadata": {},
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
          "status": {}
        },
        {
          "kind": "DeploymentConfig",
          "apiVersion": "v1",
          "metadata": {},
          "spec": {
            "strategy": {
              "type": "Rolling",
              "rollingParams": {
                "updatePeriodSeconds": 1,
                "intervalSeconds": 1,
                "timeoutSeconds": 600,
                "maxUnavailable": "25%",
                "maxSurge": "25%"
              },
              "resources": {},
              "activeDeadlineSeconds": 21600
            },
            "triggers": [],
            "replicas": 1,
            "test": false,
            "selector": {},
            "template": {
              "metadata": {},
              "spec": {
                "containers": [
                  {
                    "name": "dnsmasq",
                    "image": "andyshinn/dnsmasq@sha256:e219b6a321579580aad06782f048dddb907ea990f86231ca0517a406853dc2eb",
                    "ports": [],
                    "resources": {},
                    "terminationMessagePath": "/dev/termination-log",
                    "imagePullPolicy": "Always"
                  }
                ],
                "restartPolicy": "Always",
                "terminationGracePeriodSeconds": 30,
                "dnsPolicy": "ClusterFirst",
                "securityContext": {}
              }
            }
          },
          "status": null
        }
      ]}"""

      val expected: List[TopLevel] = List(
        Route(
          metadata = Some(ObjectMeta.empty),
          spec = RouteSpec(
            host = "dnsmasq-myproject.192.168.42.131.nip.io",
            to = RouteTargetReference(
              kind = "Service",
              name = "dnsmasq"
            )
          )
        ),
        DeploymentConfig(
          metadata = Some(ObjectMeta.empty),
          spec = DeploymentConfigSpec(
            strategy = DeploymentStrategy("Rolling"),
            triggers = Nil,
            replicas = 1,
            test = false,
            template = Some(
              PodTemplateSpec(
                metadata = Some(ObjectMeta.empty),
                spec = PodSpec(
                  volumes = None,
                  containers = List(Container(
                    image = ImageName(
                      "andyshinn/dnsmasq@sha256:e219b6a321579580aad06782f048dddb907ea990f86231ca0517a406853dc2eb"),
                    imagePullPolicy = "Always",
                    args = None,
                    command = None,
                    env = None
                  ))
                )
              ))
          ),
          status = None
        )
      )

      j.hcursor.downField("items").as[List[TopLevel]] should equal(Right(expected))
    }
  }

  "Mixed Oapi & K8s TopLevel" - {
    "should be creatable from a list of items" in {
      val j: Json = json"""{
      "items": [
        {
          "kind": "Route",
          "apiVersion": "v1",
          "metadata": null,
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

      val expected: List[EitherTopLevel] = List(
        Left(
          Route(
            metadata = None,
            spec = RouteSpec(
              host = "dnsmasq-myproject.192.168.42.131.nip.io",
              to = RouteTargetReference(
                kind = "Service",
                name = "dnsmasq"
              )
            )
          )),
        Right(
          Service(
            metadata = None,
            spec = ServiceSpec()
          ))
      )

      j.hcursor.downField("items").as[List[EitherTopLevel]] should equal(Right(expected))
    }
  }

}
