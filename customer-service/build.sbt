name := """customer-service"""
organization := "mdaros.training.play"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.3"
//scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += "com.typesafe.play" %% "play-slick" % "5.0.0"
libraryDependencies += "com.typesafe.slick" %% "slick-codegen" % "3.3.3"
libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3"
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.11"
libraryDependencies += "org.mindrot" % "jbcrypt" % "0.4"

// Test dependencies
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "mdaros.training.play.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "mdaros.training.play.binders._"

// Set http port to 8080 in DEV mode
PlayKeys.devSettings := Seq ( "play.server.http.port" -> "8080" )