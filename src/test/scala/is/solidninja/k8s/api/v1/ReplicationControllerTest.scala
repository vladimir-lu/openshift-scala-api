package is.solidninja
package k8s
package api
package v1

import java.time.{ZoneId, ZonedDateTime}

import org.scalatest.{FreeSpec, Matchers}
import io.circe._
import io.circe.literal._
import io.circe.syntax._

class ReplicationControllerTest extends FreeSpec with Matchers {

  import JsonProtocol._

  "ReplicationController v1" - {

    "should encode/decode based on a simple example" in {
      val j: Json = json"""{
        "kind": "ReplicationController",
        "apiVersion": "v1",
        "metadata": {
          "name": "jenkins-1",
          "namespace": "myproject",
          "selfLink": "/api/v1/namespaces/myproject/replicationcontrollers/jenkins-1",
          "uid": "1829eff6-864e-11e7-a9ee-a434d9a39062",
          "resourceVersion": "787",
          "generation": 1,
          "creationTimestamp": "2017-08-21T08:52:42Z",
          "labels": {
            "app": "jenkins-ephemeral",
            "openshift.io/deployment-config.name": "jenkins",
            "template": "jenkins-ephemeral-template"
          },
          "annotations": {
            "kubectl.kubernetes.io/desired-replicas": "1",
            "openshift.io/deployer-pod.name": "jenkins-1-deploy",
            "openshift.io/deployment-config.latest-version": "1",
            "openshift.io/deployment-config.name": "jenkins",
            "openshift.io/deployment.phase": "Failed",
            "openshift.io/deployment.replicas": "0",
            "openshift.io/deployment.status-reason": "image change"
          }
        },
        "spec": {
          "replicas": 0,
          "selector": {
            "deployment": "jenkins-1",
            "deploymentconfig": "jenkins",
            "name": "jenkins"
          },
          "template": {
            "spec": {
              "volumes": [],
              "containers": [
                {
                  "name": "jenkins",
                  "image": "openshift/jenkins-2-centos7@sha256:5faaf75c1652aac7f8feda5299a2316b51d541d06afd51cacb575ce98b5de3c9",
                  "resources": {},
                  "imagePullPolicy": "IfNotPresent"
                }
              ],
              "restartPolicy": "Never",
              "terminationGracePeriodSeconds": 10,
              "activeDeadlineSeconds": 21600,
              "dnsPolicy": "ClusterFirst",
              "serviceAccountName": "deployer",
              "nodeName": "172.22.22.60",
              "securityContext": {},
              "imagePullSecrets": []
            }
          }
        },
        "status": {
          "replicas": 0,
          "observedGeneration": 1
        }
      }"""

      val expected = ReplicationController(
        metadata = Some(
          ObjectMeta(
            name = Some("jenkins-1"),
            namespace = Some(Namespace("myproject")),
            selfLink = Some(Path("/api/v1/namespaces/myproject/replicationcontrollers/jenkins-1")),
            uid = Some(Uid("1829eff6-864e-11e7-a9ee-a434d9a39062")),
            resourceVersion = Some(Version("787")),
            generation = Some(1),
            creationTimestamp = Some(Timestamp(ZonedDateTime.of(2017, 8, 21, 8, 52, 42, 0, ZoneId.of("UTC")))),
            labels = Some(
              Map(
                "app" -> "jenkins-ephemeral",
                "openshift.io/deployment-config.name" -> "jenkins",
                "template" -> "jenkins-ephemeral-template"
              )),
            annotations = Some(Annotations(Map(
              "kubectl.kubernetes.io/desired-replicas" -> Json.fromString("1"),
              "openshift.io/deployer-pod.name" -> Json.fromString("jenkins-1-deploy"),
              "openshift.io/deployment-config.latest-version" -> Json.fromString("1"),
              "openshift.io/deployment-config.name" -> Json.fromString("jenkins"),
              "openshift.io/deployment.phase" -> Json.fromString("Failed"),
              "openshift.io/deployment.replicas" -> Json.fromString("0"),
              "openshift.io/deployment.status-reason" -> Json.fromString("image change")
            )))
          )),
        spec = ReplicationControllerSpec(
          replicas = 0,
          selector = Some(
            Selector(
              Map(
                "deployment" -> Json.fromString("jenkins-1"),
                "deploymentconfig" -> Json.fromString("jenkins"),
                "name" -> Json.fromString("jenkins")
              )
            )),
          template = PodTemplateSpec(
            metadata = None,
            spec = PodSpec(
              volumes = Some(Nil),
              containers = Container(
                args = None,
                command = None,
                env = None,
                image = ImageName(
                  "openshift/jenkins-2-centos7@sha256:5faaf75c1652aac7f8feda5299a2316b51d541d06afd51cacb575ce98b5de3c9"),
                imagePullPolicy = Some("IfNotPresent"),
                name = Some("jenkins"),
                resources = Some(ResourceRequirements())
              ) :: Nil,
              restartPolicy = Some("Never"),
              terminationGracePeriodSeconds = Some(Seconds(10)),
              dnsPolicy = Some("ClusterFirst"),
              securityContext = Some(PodSecurityContext()),
              imagePullSecrets = Some(Nil),
              nodeName = Some("172.22.22.60"),
              serviceAccountName = Some("deployer"),
              activeDeadlineSeconds = Some(Seconds(21600))
            )
          )
        ),
        status = Some(
          ReplicationControllerStatus(
            replicas = 0,
            observedGeneration = 1
          )
        )
      )

      j.as[ReplicationController] should equal(Right(expected))
      j.as[ReplicationController].map(_.asJson.withoutNulls) should equal(Right(j))
    }

  }
}
