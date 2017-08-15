package is.solidninja
package k8s
package api
package v1

import org.scalatest.{FreeSpec, Matchers}

import io.circe._
import io.circe.literal._
import io.circe.syntax._

import JsonProtocol._

class ContainerTest extends FreeSpec with Matchers {

  "Container v1" - {
    "should decode and reencode from a simple example" in {
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
        "resources": {
          "requests": {
            "cpu": "500m",
            "memory": "1Gi"
          }
        },
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
        name = Some("deployment"),
        env = Some(
          List(
            EnvVar("KUBERNETES_MASTER", "https://172.22.22.60:8443"),
            EnvVar("OPENSHIFT_DEPLOYMENT_NAMESPACE", "myproject")
          )),
        image = ImageName("openshift/origin-deployer:v1.5.1"),
        imagePullPolicy = Some("IfNotPresent"),
        resources = Some(
          ResourceRequirements(
            requests = Some(
              CpuMemory(
                cpu = Some("500m"),
                memory = Some("1Gi")
              ))
          )),
        terminationMessagePath = Some("/dev/termination-log"),
        volumeMounts = Some(
          List(
            VolumeMount(mountPath = "/var/run/secrets/kubernetes.io/serviceaccount",
                        name = "deployer-token-rtf4m",
                        readOnly = Some(true))
          )),
        securityContext = Some(
          SecurityContext(
            capabilities = Some(
              Capabilities(
                drop =
                  Some(Capability("KILL") :: Capability("MKNOD") :: Capability("SETGID") :: Capability("SETUID") ::
                    Capability("SYS_CHROOT") :: Nil),
                add = None
              )),
            privileged = Some(false),
            seLinuxOptions = Some(
              SELinuxOptions(
                level = Some("s0:c6,c5")
              )),
            runAsUser = Some(1000040000L)
          ))
      )

      j.as[Container] should equal(Right(expected))
      j.as[Container].map(_.asJson.withoutNulls) should equal(Right(j))
    }

    "should encode from a simple example" in {
      val container = Container(
        args = None,
        command = None,
        env = Some(
          List(
            EnvVar("KUBERNETES_MASTER", "https://172.22.22.60:8443"),
            EnvVar("OPENSHIFT_DEPLOYMENT_NAMESPACE", "myproject")
          )),
        image = ImageName("openshift/origin-deployer:v1.5.1"),
        imagePullPolicy = Some("IfNotPresent")
      )

      container.asJson.withoutNulls should equal(json"""{
        "image" : "openshift/origin-deployer:v1.5.1",
        "imagePullPolicy" : "IfNotPresent",
        "env" : [
          {
            "name" : "KUBERNETES_MASTER",
            "value" : "https://172.22.22.60:8443"
          },
          {
            "name" : "OPENSHIFT_DEPLOYMENT_NAMESPACE",
            "value" : "myproject"
          }
        ]
      }""")
      container.asJson.as[Container] should equal(Right(container))
    }
  }
}
