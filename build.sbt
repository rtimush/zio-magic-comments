lazy val versions = new {
  val scala    = "2.13.5"
  val scalafix = "0.9.26"
  val zioMagic = "0.1.12"
}

ThisBuild / organization := "com.timushev"
ThisBuild / scalaVersion := versions.scala
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val root = project
  .in(file("."))
  .aggregate(rules, input, output, tests)
  .settings(
    publish / skip := true
  )

lazy val rules = project
  .settings(
    name := "zio-magic-comments",
    libraryDependencies ++= Seq(
      "ch.epfl.scala"        %% "scalafix-core" % versions.scalafix,
      "io.github.kitlangton" %% "zio-magic"     % versions.zioMagic
    )
  )

lazy val input = project
  .disablePlugins(ScalafixPlugin)
  .settings(
    publish / skip := true,
    libraryDependencies ++= Seq(
      "io.github.kitlangton" %% "zio-magic" % versions.zioMagic
    )
  )

lazy val output = project
  .disablePlugins(ScalafixPlugin)
  .settings(
    publish / skip := true,
    libraryDependencies ++= Seq(
      "io.github.kitlangton" %% "zio-magic" % versions.zioMagic
    )
  )

lazy val tests = project
  .enablePlugins(ScalafixTestkitPlugin)
  .dependsOn(rules)
  .settings(
    publish / skip := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % versions.scalafix % Test cross CrossVersion.full,
    Compile / compile :=
      (Compile / compile).dependsOn(input / Compile / compile).value,
    scalafixTestkitOutputSourceDirectories := (output / Compile / unmanagedSourceDirectories).value,
    scalafixTestkitInputSourceDirectories := (input / Compile / unmanagedSourceDirectories).value,
    scalafixTestkitInputScalacOptions := (input / Compile / scalacOptions).value,
    scalafixTestkitInputClasspath := (input / Compile / fullClasspath).value
  )
