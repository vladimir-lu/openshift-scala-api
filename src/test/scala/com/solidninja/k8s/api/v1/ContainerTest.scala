package com.solidninja.k8s.api.v1

import org.scalatest.{FreeSpec, Matchers}
import io.circe._
import io.circe.literal._

import Decoders._

class ContainerTest extends FreeSpec with Matchers {

  "Container v1" - {
    "should decode from a simple example" in {
      val j: Json = json"""{
        "name": "deployment",
        "image": "openshift/origin-deployer:v1.5.1",
        "env": [
          {
            "name": "KUBERNETES_MASTER",
            "value": "https://172.22.22.60:8443"
          },
          {
            "name": "OPENSHIFT_DEPLOYMENT_NAMESPACE",
            "value": "myproject"
          }
        ],
        "resources": {},
        "volumeMounts": [
          {
            "name": "deployer-token-rtf4m",
            "readOnly": true,
            "mountPath": "/var/run/secrets/kubernetes.io/serviceaccount"
          }
        ],
        "terminationMessagePath": "/dev/termination-log",
        "imagePullPolicy": "IfNotPresent",
        "securityContext": {
          "capabilities": {
            "drop": [
              "KILL",
              "MKNOD",
              "SETGID",
              "SETUID",
              "SYS_CHROOT"
            ]
          },
          "privileged": false,
          "seLinuxOptions": {
            "level": "s0:c6,c5"
          },
          "runAsUser": 1000040000
        }
      }"""

      val expected = Container(
        args = None,
        command = None,
        env = Some(List(
          EnvVar("KUBERNETES_MASTER", "https://172.22.22.60:8443"),
          EnvVar("OPENSHIFT_DEPLOYMENT_NAMESPACE", "myproject")
        )),
        image = ImageName("openshift/origin-deployer:v1.5.1"),
        imagePullPolicy = "IfNotPresent"
      )

      j.as[Container] should equal(Right(expected))
    }
  }
}
