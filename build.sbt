ThisBuild / version := "0.1"

ThisBuild / scalaVersion := "2.13.10"

val tapirVersion = "1.0.2"
val ZIOVersion = "2.0.13"
val ZIOConfigVersion = "3.0.1"

lazy val root = (project in file("."))
  .settings(
    name := "amazon-price-history",
    libraryDependencies ++= Seq(
      "io.getquill" %% "quill-jdbc-zio" % "4.6.0",
      "org.postgresql" % "postgresql" % "42.3.1",
      "org.jsoup" % "jsoup" % "1.16.1",

      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-zio" % tapirVersion,

      "dev.zio" %% "zio-http" % "3.0.0-RC1",
      "dev.zio" %% "zio" % ZIOVersion,
      "dev.zio" %% "zio-config" % ZIOConfigVersion,
      "dev.zio" %% "zio-config-typesafe" % ZIOConfigVersion,
      "dev.zio" %% "zio-config-magnolia" % ZIOConfigVersion
    ),
    scalacOptions ++= Seq("-Ymacro-annotations"),
  )
