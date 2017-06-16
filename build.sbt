
val libraries = Seq(
  "org.http4s" %% "http4s-blaze-client" % "0.17.0-M3",
  "org.http4s" %% "http4s-circe" % "0.17.0-M3",
  "co.fs2" %% "fs2-core" % "0.9.7",
  // Runtime
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime"
)

lazy val root = Project(
  id = "openshift-client",
  base = file("."),
  settings = Seq(
    scalaVersion := "2.11.11",
    libraryDependencies ++= libraries
  )
)
