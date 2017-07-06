
val libraries = Seq(
  "org.http4s" %% "http4s-blaze-client" % "0.17.0-M3",
  "org.http4s" %% "http4s-circe" % "0.17.0-M3",
  "io.circe" %% "circe-literal" % "0.8.0",
  "io.circe" %% "circe-generic" % "0.8.0",
  "co.fs2" %% "fs2-core" % "0.9.7",
  // Test
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
// Runtime
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime"
)

lazy val root = Project(
  id = "openshift-client",
  base = file("."),
  settings = Seq(
    organization := "is.solidninja.openshift",
    scalaVersion := "2.11.11",
    libraryDependencies ++= libraries
  )
)
