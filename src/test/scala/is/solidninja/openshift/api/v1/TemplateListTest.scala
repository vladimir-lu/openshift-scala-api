package is.solidninja
package openshift
package api
package v1

import org.scalatest.{FreeSpec, Matchers}

import io.circe.literal._

import Decoders._

class TemplateListTest extends FreeSpec with Matchers {

  "TemplateList should" - {
    "decode into a blank list of items when empty" in {
      val j = json"""{
        "kind": "List",
        "apiVersion": "v1",
        "metadata": {},
        "items": []
      }"""

      j.as[TemplateList] should equal(Right(TemplateList(items = Nil)))
    }
  }

}
