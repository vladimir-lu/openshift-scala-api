import sbt._
import Keys._

import com.typesafe.sbt.SbtPgp.autoImport._

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
    description := "API client for OpenShift Origin/Kubernetes",
    homepage := Some(url("https://github.com/solidninja/openshift-scala-api")),
    startYear := Some(2017),
    licenses += "MIT" -> url("https://opensource.org/licenses/mit-license.php"),
    developers := List(
      Developer(
        id = "vladimir-lu",
        name = "Vladimir Lushnikov",
        email = "vladimir@solidninja.is",
        url = url("https://solidninja.is")
      )
    ),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/solidninja/openshift-scala-api"),
        "scm:git:https://github.com/solidninja/openshift-scala-api.git",
        Some(s"scm:git:git@github.com:solidninja/openshift-scala-api.git")
      ))
  )

  val commonSettings = mavenCentralSettings ++ Seq(
    organization := "is.solidninja.openshift",
    version := "0.0.8-SNAPSHOT",
    scalaVersion := "2.12.3",
    crossScalaVersions := Seq("2.12.3", "2.11.11"),
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

  val testSettings = Seq(
    testOptions in Test ++= List(
      Tests.Argument(TestFrameworks.ScalaTest,
        "-y", "org.scalatest.FreeSpec",
        "-l", "LocalCluster")
    )
  )

  val publishSettings = Seq(
    publishTo := Some(
      if (isSnapshot.value)
        Opts.resolver.sonatypeSnapshots
      else
        Opts.resolver.sonatypeStaging
    ),
    publishMavenStyle := true,
    publishArtifact in Test := false
  )
}
