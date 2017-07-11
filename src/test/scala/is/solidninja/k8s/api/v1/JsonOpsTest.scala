package is.solidninja.k8s.api.v1

import org.scalatest.{FreeSpec, Matchers}

import io.circe.literal._

class JsonOpsTest extends FreeSpec with Matchers {

  object ops extends JsonOps
  import ops._

  "JsonOps" - {
    "should not remove nulls" - {

      "when the json is a null value" in {
        val json = json"""null"""
        json.withoutNulls should equal(json)
      }

      "when the json is an array of nulls" in {
        val json = json"""[null, 1, 2, null]"""
        json.withoutNulls should equal(json)
      }

      "when the json is a deeply nested array of nulls" in {
        val json = json"""{"arr": [null, {"a": 1}, null]}"""
        json.withoutNulls should equal(json)
      }
    }

    "should remove nulls" - {

      "when the json is an object with top-level null values" in {
        val json = json"""{
          "a": null,
          "b": 42,
          "c": null,
          "d": "bar"
        }"""

        json.withoutNulls should equal(json"""{"b": 42, "d": "bar"}""")
      }

      "when the json is an object with deeply nested null values inside objects" in {
        val json = json"""{
          "a": {
            "b": null,
            "c": [null, null],
            "d": {"e": null},
            "f": {"g": "h"}
          }
        }"""

        json.withoutNulls should equal(json"""{"a": {"c": [null, null], "d": {}, "f": {"g": "h"}}}""")
      }

      "when there is an array of json objects deeply nested" in {
        val json = json"""[
          null,
          {"a": null, "b": "foo"},
          {"c": {"d": null}}
        ]"""

        json.withoutNulls should equal(json"""[null, {"b": "foo"}, {"c": {}}]""")
      }

      "when there is a null value inside an object of nested arrays" in {
        val json = json"""{ "a": [ {"b": "c", "d": null} ] }"""
        json.withoutNulls should equal(json"""{"a": [ {"b": "c"} ] }""")
      }

    }
  }

}
