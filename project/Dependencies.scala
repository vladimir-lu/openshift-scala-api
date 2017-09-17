import sbt._

object Dependencies {
  val http4s = Seq(
    "org.http4s" %% "http4s-circe" % "0.18.0-M1",
    "org.http4s" %% "http4s-client" % "0.18.0-M1"
  )

  val diffson = Seq(
    "org.gnieh" %% "diffson-circe" % "2.2.2"
  )

  val circe = Seq(
    "io.circe" %% "circe-literal" % "0.9.0-M1",
    "io.circe" %% "circe-generic" % "0.9.0-M1"
  )

  val fs2 = Seq(
    "co.fs2" %% "fs2-core" % "0.10.0-M6"
  )

  val scalatest = Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  )

  val testBlazeHttp = Seq(
    "org.http4s" %% "http4s-blaze-client" % "0.18.0-M1" % "test"
  )

  val testLogging = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3" % "test"
  )
}
