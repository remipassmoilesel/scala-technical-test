name := """scala-technical-test"""

version := "0.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.7"

crossScalaVersions := Seq("2.11.12", "2.12.7")

/**
  * Dependencies
  */

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "com.h2database" % "h2" % "1.4.197"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += specs2 % Test

/**
  * Assembly plugin configuration (Fatjar packaging)
  */

mainClass in assembly := Some("play.core.server.ProdServerStart")
fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)

assemblyMergeStrategy in assembly := {
  case manifest if manifest.contains("MANIFEST.MF") =>
    MergeStrategy.discard
  case referenceOverrides if referenceOverrides.contains("reference-overrides.conf") =>
    MergeStrategy.concat
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}