package is.solidninja
package k8s
package api
package v1

import org.scalatest.{FreeSpec, Matchers}

import io.circe._
import io.circe.literal._
import io.circe.syntax._

import JsonProtocol._
import PodSpecTest._

class PodSpecTest extends FreeSpec with Matchers {

  "PodSpec v1" - {
    "should decode and reencode for a simple example" in {

      val j: Json = json"""{
        "volumes": [],
        "containers": [
          {
            "name": "deployment",
            "image": "openshift/origin-deployer:v1.5.1",
            "resources": {},
            "imagePullPolicy": "IfNotPresent"
          }
        ],
        "restartPolicy": "Never",
        "terminationGracePeriodSeconds": 10,
        "activeDeadlineSeconds": 21600,
        "dnsPolicy": "ClusterFirst",
        "serviceAccountName": "deployer",
        "serviceAccount": "deployer",
        "nodeName": "172.22.22.60",
        "securityContext": {
          "seLinuxOptions": {
            "level": "s0:c6,c5"
          },
          "fsGroup": 1000040000
        },
        "imagePullSecrets": []
      }"""

      val expected = PodSpec(
        volumes = Some(Nil),
        containers = Container(
          args = None,
          command = None,
          env = None,
          image = ImageName("openshift/origin-deployer:v1.5.1"),
          imagePullPolicy = "IfNotPresent",
          name = Some("deployment"),
          resources = Some(ResourceRequirements())
        ) :: Nil,
        restartPolicy = Some("Never"),
        terminationGracePeriodSeconds = Some(Seconds(10)),
        dnsPolicy = Some("ClusterFirst"),
        securityContext = Some(
          PodSecurityContext(
            fsGroup = Some(1000040000L),
            seLinuxOptions = Some(
              SELinuxOptions(
                level = Some("s0:c6,c5")
              ))
          )),
        imagePullSecrets = Some(Nil),
        nodeName = Some("172.22.22.60"),
        serviceAccountName = Some("deployer"),
        activeDeadlineSeconds = Some(Seconds(21600))
      )

      j.as[PodSpec] should equal(Right(expected))
      j.as[PodSpec].map(_.asJson.withoutNulls) should equal(Right(removeDeprecatedFields(j)))
    }
  }

}

object PodSpecTest {
  def removeDeprecatedFields(j: Json): Json =
    j.hcursor.downField("serviceAccount").delete.top.getOrElse(j)
}
