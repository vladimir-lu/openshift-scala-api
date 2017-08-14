package is.solidninja.k8s.api.v1

import org.scalatest.{FreeSpec, Matchers}

import io.circe._
import io.circe.literal._
import io.circe.syntax._

import JsonProtocol._

class ProbeTest extends FreeSpec with Matchers {

  "Probe " - {

    "should decode and reencode from a simple example" in {
      val j: Json = json"""{
        "httpGet": {
          "path": "/health",
          "port": 8080
        },
        "initialDelaySeconds": 60,
        "periodSeconds": 120,
        "failureThreshold": 2
      }"""

      j.as[Probe] should equal(
        Right(Probe(
          httpGet = Some(HTTPGetAction(
            path = "/health",
            port = Port(8080)
          )),
          initialDelaySeconds = Some(Seconds(60)),
          periodSeconds = Some(Seconds(120)),
          failureThreshold = Some(2)
        )))

      j.as[Probe].map(_.asJson.withoutNulls) should equal(Right(j))
    }

    "should decode from an example that mixes strings and ints for durations and thresholds" in {
      val j: Json = json"""{
        "httpGet": {
          "path": "/health",
          "port": 8080
        },
        "initialDelaySeconds": 60,
        "periodSeconds": "120",
        "failureThreshold": "2"
      }"""

      j.as[Probe] should equal(
        Right(Probe(
          httpGet = Some(HTTPGetAction(
            path = "/health",
            port = Port(8080)
          )),
          initialDelaySeconds = Some(Seconds(60)),
          periodSeconds = Some(Seconds(120)),
          failureThreshold = Some(2)
        )))
    }
  }
}
