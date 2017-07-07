import sbt._
import Keys._
import xml.Group

object build {

  val manifestSetting = packageOptions += {
    val (title, v, vendor) = (name.value, version.value, organization.value)
    Package.ManifestAttributes(
      "Created-By" -> "Simple Build Tool",
      "Built-By" -> System.getProperty("user.name"),
      "Build-Jdk" -> System.getProperty("java.version"),
      "Specification-Title" -> title,
      "Specification-Version" -> v,
      "Specification-Vendor" -> vendor,
      "Implementation-Title" -> title,
      "Implementation-Version" -> v,
      "Implementation-Vendor-Id" -> vendor,
      "Implementation-Vendor" -> vendor
    )
  }

  val mavenCentralSettings = Seq(
    homepage := Some(new URL("https://github.com/json4s/json4s")),
    startYear := Some(2017),
    licenses := Seq(("MIT", new URL("https://opensource.org/licenses/mit-license.php"))),
    pomExtra := {
      pomExtra.value ++ Group(
        <scm>
          <url>https://github.com/solidninja/openshift-scala-api</url>
          <connection>scm:git:https://github.com/solidninja/openshift-scala-api</connection>
        </scm>
          <developers>
            <developer>
              <id>vladimir-lu</id>
              <name>Vladimir Lushnikov</name>
              <organization>SOLID Ninja Ltd.</organization>
              <organizationUrl>https://solidninja.is</organizationUrl>
            </developer>
          </developers>
      )
    }
  )

  val commonSettings = mavenCentralSettings ++ Seq(
    organization := "is.solidninja.openshift",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.2",
    crossScalaVersions := Seq("2.11.11", "2.12.2"),
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Xfuture",
      "-Ywarn-unused-import"
    ),
    javacOptions ++= Seq("-target", "1.8", "-source", "1.8"),
    manifestSetting,
    crossVersion := CrossVersion.binary
  )
}