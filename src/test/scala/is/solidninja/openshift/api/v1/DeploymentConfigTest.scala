package is.solidninja
package openshift
package api
package v1

import java.time.{ZoneId, ZonedDateTime}

import org.scalatest.{FreeSpec, Matchers}

import io.circe._
import io.circe.literal._
import io.circe.syntax._

import JsonProtocol._

import is.solidninja.k8s.api.v1._

class DeploymentConfigTest extends FreeSpec with Matchers {

  "DeploymentConfig v1" - {
    "should decode and reencode based on a simple example" in {
      val j: Json = json"""{
      "kind": "DeploymentConfig",
      "apiVersion": "v1",
      "metadata": {
        "name": "dnsmasq",
        "namespace": "myproject",
        "selfLink": "/oapi/v1/namespaces/myproject/deploymentconfigs/dnsmasq",
        "uid": "823ede65-5c06-11e7-ab5a-c26606f097c1",
        "resourceVersion": "904",
        "generation": 2,
        "creationTimestamp": "2017-06-28T13:34:28Z",
        "labels": {
          "app": "dnsmasq"
        },
        "annotations": {
          "openshift.io/generated-by": "OpenShiftWebConsole"
        }
      },
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
        "triggers": [
          {
            "type": "ConfigChange"
          },
          {
            "type": "ImageChange",
            "imageChangeParams": {
              "automatic": true,
              "containerNames": [
                "dnsmasq"
              ],
              "from": {
                "kind": "ImageStreamTag",
                "namespace": "myproject",
                "name": "dnsmasq:latest"
              },
              "lastTriggeredImage": "andyshinn/dnsmasq@sha256:e219b6a321579580aad06782f048dddb907ea990f86231ca0517a406853dc2eb"
            }
          }
        ],
        "replicas": 1,
        "test": false,
        "selector": {
          "app": "dnsmasq",
          "deploymentconfig": "dnsmasq"
        },
        "template": {
          "metadata": {
            "labels": {
              "app": "dnsmasq",
              "deploymentconfig": "dnsmasq"
            },
            "annotations": {
              "openshift.io/generated-by": "OpenShiftWebConsole"
            }
          },
          "spec": {
            "containers": [
              {
                "name": "dnsmasq",
                "image": "andyshinn/dnsmasq@sha256:e219b6a321579580aad06782f048dddb907ea990f86231ca0517a406853dc2eb",
                "ports": [
                  {
                    "containerPort": 53,
                    "protocol": "TCP"
                  },
                  {
                    "containerPort": 53,
                    "protocol": "UDP"
                  }
                ],
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
      "status": {
        "latestVersion": 1,
        "observedGeneration": 2,
        "replicas": 0,
        "updatedReplicas": 0,
        "availableReplicas": 0,
        "unavailableReplicas": 0,
        "details": {
          "message": "image change",
          "causes": [
            {
              "type": "ImageChange",
              "imageTrigger": {
                "from": {
                  "kind": "ImageStreamTag",
                  "namespace": "myproject",
                  "name": "dnsmasq:latest"
                }
              }
            }
          ]
        },
        "conditions": [
          {
            "type": "Available",
            "status": "False",
            "lastUpdateTime": "2017-06-28T13:34:28Z",
            "lastTransitionTime": "2017-06-28T13:34:28Z",
            "message": "Deployment config does not have minimum availability."
          },
          {
            "type": "Progressing",
            "status": "False",
            "lastUpdateTime": "2017-06-28T13:44:43Z",
            "lastTransitionTime": "2017-06-28T13:44:43Z",
            "reason": "ProgressDeadlineExceeded",
            "message": "replication controller \"dnsmasq-1\" has failed progressing"
          }
        ]
      }
    }"""

      val expected = DeploymentConfig(
        spec = DeploymentConfigSpec(
          strategy = DeploymentStrategy(
            `type` = "Rolling",
            rollingParams = Some(
              RollingDeploymentStrategyParams(
                updatePeriodSeconds = Some(Seconds(1)),
                intervalSeconds = Some(Seconds(1)),
                timeoutSeconds = Some(Seconds(600)),
                maxUnavailable = Some("25%"),
                maxSurge = Some("25%")
              )),
            labels = None,
            annotations = None,
            resources = Some(ResourceRequirements()),
            activeDeadlineSeconds = Some(Seconds(21600))
          ),
          triggers = List(
            DeploymentTriggerPolicy(`type` = "ConfigChange", imageChangeParams = None),
            DeploymentTriggerPolicy(
              `type` = "ImageChange",
              imageChangeParams = Some(DeploymentTriggerImageChangeParams(
                automatic = Some(true),
                containerNames = List("dnsmasq"),
                from = ObjectReference(
                  kind = Some("ImageStreamTag"),
                  namespace = Some("myproject"),
                  name = Some("dnsmasq:latest"),
                  uid = None,
                  apiVersion = None,
                  resourceVersion = None,
                  fieldPath = None
                ),
                lastTriggeredImage = Some(ImageName(
                  "andyshinn/dnsmasq@sha256:e219b6a321579580aad06782f048dddb907ea990f86231ca0517a406853dc2eb"))
              ))
            )
          ),
          replicas = 1,
          test = false,
          template = Some(
            PodTemplateSpec(
              metadata = Some(ObjectMeta(
                name = None,
                namespace = None,
                labels = Some(Map(
                  "app" -> "dnsmasq",
                  "deploymentconfig" -> "dnsmasq"
                )),
                annotations = Some(Annotations(
                  Map("openshift.io/generated-by" -> Json.fromString("OpenShiftWebConsole"))
                ))
              )),
              spec = PodSpec(
                volumes = None,
                containers = List(
                  Container(
                    image = ImageName(
                      "andyshinn/dnsmasq@sha256:e219b6a321579580aad06782f048dddb907ea990f86231ca0517a406853dc2eb"),
                    imagePullPolicy = Some("Always"),
                    args = None,
                    command = None,
                    env = None,
                    name = Some("dnsmasq"),
                    ports = Some(
                      List(
                        ContainerPort(containerPort = 53, protocol = Some("TCP")),
                        ContainerPort(containerPort = 53, protocol = Some("UDP"))
                      )),
                    resources = Some(ResourceRequirements()),
                    terminationMessagePath = Some("/dev/termination-log")
                  )
                ),
                restartPolicy = Some("Always"),
                terminationGracePeriodSeconds = Some(Seconds(30)),
                securityContext = Some(PodSecurityContext()),
                dnsPolicy = Some("ClusterFirst")
              )
            )),
          selector =
            Some(Selector(Map("app" -> Json.fromString("dnsmasq"), "deploymentconfig" -> Json.fromString("dnsmasq"))))
        ),
        status =
          Some(DeploymentConfigStatus(latestVersion = Some(1), observedGeneration = Some(2), replicas = Some(0))),
        metadata = Some(
          ObjectMeta(
            name = Some("dnsmasq"),
            namespace = Some(Namespace("myproject")),
            labels = Some(Map("app" -> "dnsmasq")),
            annotations = Some(
              Annotations(Map("openshift.io/generated-by" -> Json.fromString("OpenShiftWebConsole")))
            ),
            uid = Some(Uid("823ede65-5c06-11e7-ab5a-c26606f097c1")),
            resourceVersion = Some(Version("904")),
            creationTimestamp = Some(Timestamp(ZonedDateTime.of(2017, 6, 28, 13, 34, 28, 0, ZoneId.of("UTC")))),
            selfLink = Some(Path("/oapi/v1/namespaces/myproject/deploymentconfigs/dnsmasq")),
            generation = Some(2)
          ))
      )

      j.as[DeploymentConfig] should equal(Right(expected))

      // FIXME - status field
      j.as[DeploymentConfig].map(_.copy(status = None).asJson.withoutNulls) should equal(
        Right(j.hcursor.downField("status").delete.top.get))
    }
  }

}
