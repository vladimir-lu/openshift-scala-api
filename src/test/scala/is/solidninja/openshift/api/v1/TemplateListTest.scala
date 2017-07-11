package is.solidninja
package openshift
package api
package v1

import org.scalatest.{FreeSpec, Matchers}

import io.circe.literal._
import io.circe.syntax._

import is.solidninja.k8s.api.v1._

import JsonProtocol._

class TemplateListTest extends FreeSpec with Matchers {

  "TemplateList should" - {
    "decode into a blank list of items when empty" in {
      val j = json"""{
        "kind": "List",
        "apiVersion": "v1",
        "metadata": {},
        "items": [
          {
            "kind" : "Service",
            "apiVersion" : "v1",
            "metadata" : {
              "name" : "dnsmasq"
            },
            "spec" : {
              "type" : "ClusterIP"
            }
          }
        ]
      }"""

      val service = Service(
        metadata = Some(
          ObjectMeta(
            name = Some("dnsmasq")
          )),
        spec = ServiceSpec(
          `type` = "ClusterIP"
        )
      )
      val expected = TemplateList(
        items = Right(service) :: Nil,
        metadata = Some(ObjectMeta())
      )

      j.as[TemplateList] should equal(Right(expected))
      j.as[TemplateList].map(_.asJson.withoutNulls) should equal(Right(j))
    }
  }

}
