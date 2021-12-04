import sbt.Def
import sbtrelease.ReleaseStateTransformations._

name := "scala-macro-tools"
organization := "io.github.jxnu-liguobin"

lazy val scala212 = "2.12.14"
lazy val scala211 = "2.11.12"
lazy val scala213 = "2.13.6"
lazy val lastVersionForExamples = "0.3.0"

scalaVersion := scala213

lazy val supportedScalaVersions = List(scala213, scala212, scala211)

lazy val root = (project in file("."))
  .settings(
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "com.alipay.sofa" % "jraft-core" % "1.3.8",
      "io.grpc" % "grpc-protobuf" % "1.42.1",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
      "org.apache.logging.log4j" % "log4j-api" % "2.14.1" % Test,
      "org.apache.logging.log4j" % "log4j-core" % "2.14.1" % Test,
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.14.1" % Test,
      "com.typesafe.play" %% "play-json" % "2.7.4" % Test,
      "org.scalatest" %% "scalatest" % "3.2.10" % Test,
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.0" %Test
    ), Compile / scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n <= 12 => Nil
        case _ => List("-Ymacro-annotations" /*, "-Ymacro-debug-verbose"*/)
      }
    } ++ Seq("-language:experimental.macros"),
    organizationName := "jxnu-liguobin && contributors",
    startYear := Some(2021),
    licenses += ("MIT", new URL("https://github.com/jxnu-liguobin/scala-macro-tools/blob/master/LICENSE")),
    Test / testOptions += Tests.Argument("-oDF"),
    releaseIgnoreUntrackedFiles := true,
    releaseCrossBuild := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      releaseStepCommandAndRemaining("+compile"),
      releaseStepCommandAndRemaining("test"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publishSigned"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  ).settings(Publishing.publishSettings).settings(paradise()).enablePlugins(AutomateHeaderPlugin)
//  enablePlugins(ProtocPlugin)


lazy val `scala2-13` = (project in file("examples/scala2-13")).settings(scalaVersion := scala213)
  .settings(libraryDependencies ++= Seq(
    "io.github.jxnu-liguobin" %% "scala-macro-tools" % lastVersionForExamples,
  )).settings(
  publish / skip := true,
  Compile / scalacOptions += "-Ymacro-annotations"
)

lazy val `scala2-12` = (project in file("examples/scala2-12")).settings(scalaVersion := scala212)
  .settings(libraryDependencies ++= Seq(
    "io.github.jxnu-liguobin" %% "scala-macro-tools" % lastVersionForExamples,
  )).settings(
  publish / skip := true,
  paradise()
)

lazy val `scala2-11` = (project in file("examples/scala2-11")).settings(scalaVersion := scala211)
  .settings(libraryDependencies ++= Seq(
    "io.github.jxnu-liguobin" %% "scala-macro-tools" % lastVersionForExamples,
  )).settings(
  publish / skip := true,
  paradise()
)

def paradise(): Def.Setting[Seq[ModuleID]] = {
  libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n < 13 => Some("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
    case _ => None
  }).fold(Seq.empty[ModuleID])(f => Seq(compilerPlugin(f)))
}

// Only to import, and every thing in /intellij-plugin.
lazy val `intellij-plugin` = (project in file("intellij-plugin")).settings(publish / skip := true)
