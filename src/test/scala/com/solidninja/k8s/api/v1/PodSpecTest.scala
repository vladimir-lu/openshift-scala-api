package com.solidninja.k8s.api.v1

import java.time.{ZoneId, ZonedDateTime}

import org.scalatest.{FreeSpec, Matchers}
import io.circe._
import io.circe.literal._

import Decoders._

class PodSpecTest extends FreeSpec with Matchers {

  "PodSpec v1" - {
    "should decode for a simple example" in {

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
          imagePullPolicy = "IfNotPresent"
        ) :: Nil
      )

      j.as[PodSpec] should equal(Right(expected))
    }
  }

}
