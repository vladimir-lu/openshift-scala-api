package is.solidninja
package k8s
package api
package v1

import org.scalatest.{FreeSpec, Matchers}

import io.circe.literal._

import Decoders._

class EnvVarTest extends FreeSpec with Matchers {

  "EnvVar should" - {
    "be decodable from name/value pair" in {
      val j = json"""{
        "name": "TEST",
        "value": "world"
        }
      """

      j.as[EnvVar] should equal(Right(EnvVar("TEST", "world")))
    }

    "be decodable from name alone (defaulting to empty string)" in {
      val j = json"""{
        "name": "TEST"
        }
      """

      j.as[EnvVar] should equal(Right(EnvVar("TEST", "")))
    }
  }

}
