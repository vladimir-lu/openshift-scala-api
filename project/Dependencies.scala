import sbt._

object Dependencies {
  val http4s = Seq(
    "org.http4s" %% "http4s-circe" % "0.17.0-M3",
    "org.http4s" %% "http4s-client" % "0.17.0-M3"
  )

  val diffson = Seq(
    "org.gnieh" %% "diffson-circe" % "2.2.1"
  )

  val circe = Seq(
    "io.circe" %% "circe-literal" % "0.8.0",
    "io.circe" %% "circe-generic" % "0.8.0"
  )

  val fs2 = Seq(
    "co.fs2" %% "fs2-core" % "0.9.7"
  )

  val scalatest = Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  )

  val testBlazeHttp = Seq(
    "org.http4s" %% "http4s-blaze-client" % "0.17.0-M3" % "test"
  )

  val runtime = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime"
  )
}
