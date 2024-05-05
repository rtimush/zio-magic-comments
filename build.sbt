import MatrixUtils._

lazy val versions = new {
  val scala212 = "2.12.13"
  val scala213 = "2.13.14"
  val scalafix = "0.9.27"
  val zio      = "1.0.9"
  val zioMagic = "0.2.6"
}

ThisBuild / organization := "com.timushev"
ThisBuild / homepage     := Some(url("https://github.com/rtimush/zio-magic-comments"))
ThisBuild / licenses += (
  (
    "BSD 3-Clause",
    url("https://github.com/rtimush/zio-magic-comments/blob/master/LICENSE")
  )
)
ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / developers := List(
  Developer("rtimush", "Roman Timushev", "rtimush@gmail.com", url("https://github.com/rtimush"))
)
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/rtimush/zio-magic-comments"),
    "scm:git:https://github.com/rtimush/zio-magic-comments.git",
    Some("scm:git:git@github.com:rtimush/zio-magic-comments.git")
  )
)
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val root = project
  .in(file("."))
  .aggregate(rules.projectRefs: _*)
  .aggregate(input.projectRefs: _*)
  .aggregate(output.projectRefs: _*)
  .aggregate(tests.projectRefs: _*)
  .settings(
    publish / skip := true
  )

lazy val rules = projectMatrix
  .settings(
    name := "zio-magic-comments",
    libraryDependencies ++= Seq(
      "ch.epfl.scala"        %% "scalafix-core" % versions.scalafix,
      "io.github.kitlangton" %% "zio-magic"     % versions.zioMagic
    )
  )
  .jvmPlatform(scalaVersions = Seq(versions.scala213, versions.scala212))

lazy val input = projectMatrix
  .disablePlugins(ScalafixPlugin)
  .settings(
    publish / skip := true,
    scalacOptions += "-Wconf:any:s",
    libraryDependencies ++= Seq(
      "dev.zio"              %% "zio-test"  % versions.zio,
      "io.github.kitlangton" %% "zio-magic" % versions.zioMagic
    )
  )
  .jvmPlatform(scalaVersions = Seq(versions.scala213, versions.scala212))

lazy val output = projectMatrix
  .disablePlugins(ScalafixPlugin)
  .settings(
    publish / skip := true,
    scalacOptions += "-Wconf:any:s",
    libraryDependencies ++= Seq(
      "dev.zio"              %% "zio-test"  % versions.zio,
      "io.github.kitlangton" %% "zio-magic" % versions.zioMagic
    )
  )
  .jvmPlatform(scalaVersions = Seq(versions.scala213, versions.scala212))

lazy val tests = projectMatrix
  .enablePlugins(ScalafixTestkitPlugin)
  .dependsOn(rules)
  .settings(
    publish / skip := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % versions.scalafix % Test cross CrossVersion.full
  )
  .jvmPlatformDyn(
    scalaVersions = Seq(versions.scala213, versions.scala212),
    settings = { (scalaVersion: String) =>
      val inputProject  = input.jvm(scalaVersion)
      val outputProject = output.jvm(scalaVersion)
      Seq(
        Compile / compile := (Compile / compile).dependsOn(inputProject / Compile / compile).value,
        scalafixTestkitOutputSourceDirectories := (outputProject / Compile / unmanagedSourceDirectories).value,
        scalafixTestkitInputSourceDirectories := (inputProject / Compile / unmanagedSourceDirectories).value,
        scalafixTestkitInputScalacOptions := (inputProject / Compile / scalacOptions).value,
        scalafixTestkitInputClasspath     := (inputProject / Compile / fullClasspath).value
      )
    }
  )
