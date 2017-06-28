package com.solidninja.k8s.api.v1

import java.time.{ZoneId, ZonedDateTime}

import io.circe._
import io.circe.literal._

import org.scalatest.{FreeSpec, Matchers}

import Decoders._

class ObjectMetaTest extends FreeSpec with Matchers {

  "ObjectMeta v1" - {
    "should decode based on a simple example" in {
      val j: Json = json"""{
        "name": "mongodb-1-deploy",
        "namespace": "myproject",
        "selfLink": "/api/v1/namespaces/myproject/pods/mongodb-1-deploy",
        "uid": "8d9731d0-51dd-11e7-8972-a434d9a39062",
        "resourceVersion": "12034",
        "creationTimestamp": "2017-06-15T15:16:06Z",
        "labels": {
          "openshift.io/deployer-pod-for.name": "mongodb-1"
        },
        "annotations": {
          "openshift.io/deployment.name": "mongodb-1",
          "openshift.io/scc": "restricted"
        }
      }"""

      val expected = ObjectMeta(
        name = Some("mongodb-1-deploy"),
        namespace = Some(Namespace("myproject")),
        labels = Map(
          "openshift.io/deployer-pod-for.name" -> "mongodb-1"
        ),
        annotations = Annotations(
          Map(
            "openshift.io/deployment.name" -> Json.fromString("mongodb-1"),
            "openshift.io/scc" -> Json.fromString("restricted")
          )),
        uid = Some(Uid("8d9731d0-51dd-11e7-8972-a434d9a39062")),
        resourceVersion = Some(Version("12034")),
        creationTimestamp = Some(Timestamp(ZonedDateTime.of(2017, 6, 15, 15, 16, 6, 0, ZoneId.of("UTC")))),
        selfLink = Some(Path("/api/v1/namespaces/myproject/pods/mongodb-1-deploy"))
      )

      j.as[ObjectMeta] should equal(Right(expected))
    }
  }

}
