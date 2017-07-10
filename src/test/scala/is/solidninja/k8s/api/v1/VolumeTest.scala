package is.solidninja
package k8s
package api
package v1

import org.scalatest.{FreeSpec, Matchers}

import io.circe._
import io.circe.literal._

import JsonProtocol._

class VolumeTest extends FreeSpec with Matchers {

  "Volume v1" - {
    "should decode from a simple example" in {
      val j: Json = json"""{
            "name": "deployer-token-rtf4m",
            "secret": {
              "secretName": "deployer-token-rtf4m",
              "defaultMode": 420
            }
          }"""

      val expected = Volume(
        name = "deployer-token-rtf4m",
        secret = Some(
          SecretVolumeSource(
            secretName = "deployer-token-rtf4m"
          ))
      )

      j.as[Volume] should equal(Right(expected))
      // FIXME - add test for decoding back
    }
  }
}
