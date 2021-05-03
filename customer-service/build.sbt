name := """customer-service"""
organization := "mdaros.training.play"

version := "1.0-SNAPSHOT"

lazy val root = ( project in file ( "." ) ).enablePlugins ( PlayScala )

scalaVersion := "2.13.3"

libraryDependencies ++= Seq (

  guice,
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
  "com.typesafe.slick" %% "slick-codegen" % "3.3.3",
  "com.typesafe.play" %% "play-json" % "2.8.1",
  "org.postgresql" % "postgresql" % "42.2.11",
  "org.mindrot" % "jbcrypt" % "0.4",

  // Test dependencies
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
  specs2 % Test
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "mdaros.training.play.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "mdaros.training.play.binders._"

// Set http port to 8080 in DEV mode
PlayKeys.devSettings := Seq ( "play.server.http.port" -> "8080" )