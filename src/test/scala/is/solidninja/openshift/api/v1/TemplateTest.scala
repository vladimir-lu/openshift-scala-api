package is.solidninja
package openshift
package api
package v1

import java.nio.file.{Files, Paths}

import org.scalatest.{FreeSpec, Matchers}

import io.circe.syntax._
import io.circe.literal._

import JsonProtocol._

import io.circe._

class TemplateTest extends FreeSpec with Matchers {

  "A template" - {
    "should be correctly decodable and re-encodable" in {
      val j = json"""{
        "apiVersion": "v1",
        "kind": "Template",
        "labels": {
          "template": "example"
        },
        "objects": [
          {
            "apiVersion": "v1",
            "kind": "Service",
            "metadata": {
              "annotations": {
                "description": "Exposes and load balances the application pods"
              },
              "name": "$${NAME}"
            },
            "spec": {
              "ports": [
                {
                  "name": "web",
                  "port": 8080,
                  "targetPort": 8080
                }
              ],
              "selector": {
                "name": "$${NAME}"
              }
            }
          }
        ],
        "parameters": [
          {
            "description": "The name assigned to all of the frontend objects defined in this template.",
            "displayName": "Name",
            "name": "NAME",
            "required": true,
            "value": "httpd-example"
          }
        ]
      }"""

      val expected = Template(
        labels = Some(Map("template" -> "example")),
        message = None,
        metadata = None,
        objects = List(json"""{
            "apiVersion": "v1",
            "kind": "Service",
            "metadata": {
              "annotations": {
                "description": "Exposes and load balances the application pods"
              },
              "name": "$${NAME}"
            },
            "spec": {
              "ports": [
                {
                  "name": "web",
                  "port": 8080,
                  "targetPort": 8080
                }
              ],
              "selector": {
                "name": "$${NAME}"
              }
            }
          }"""),
        parameters = List(
          Parameter(
            name = "NAME",
            displayName = Some("Name"),
            description = Some("The name assigned to all of the frontend objects defined in this template."),
            value = Some("httpd-example"),
            generate = None,
            from = None,
            required = Some(true)
          ))
      )

      j.as[Template] should equal(Right(expected))
      j.as[Template].map(_.asJson.withoutNulls) should equal(Right(j))

    }
  }

  "the httpd-example template" - {
    "should expand correctly with no parameters" in {
      val template = TemplateTest.readTemplate("httpd-example")
      val j = TemplateTest.readExpanded("httpd-example")

      template.expand(Map.empty) should equal(j.as[TemplateList])

      // FIXME - full test with json comparison
//      template.expand(Map.empty).map(_.asJson) should equal(Right(j))
    }
  }
}

object TemplateTest {

  // FIXME - this was too quick and dirty

  private[v1] def readExpanded(scenario: String): Json = readJson(scenario, "expanded.json")

  private[v1] def readTemplate(scenario: String): Template =
    readJson(scenario, "template.json")
      .as[Template]
      .getOrElse(throw new RuntimeException(s"Unable to load template for scenario $scenario"))

  private[this] def readJson(scenario: String, name: String): Json = {
    val path = s"/template/$scenario/$name"
    val uri =
      Option(getClass.getResource(path))
        .map(_.toURI)
        .getOrElse(throw new RuntimeException(s"Unable to get URI of $path"))

    val s = new String(Files.readAllBytes(Paths.get(uri)))
    parser.parse(s).getOrElse(throw new RuntimeException(s"Unable to parse json at $path"))
  }

}
