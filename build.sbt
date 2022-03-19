import sbt.librarymanagement.InclExclRule
import sbt.{ Def, Test }
import sbtrelease.ReleaseStateTransformations._

ThisBuild / resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("public"),
  Resolver.sonatypeRepo("snapshots")
)

lazy val scala212 = "2.12.14"
lazy val scala211 = "2.11.12"
lazy val scala213 = "2.13.8"
lazy val lastVersionForExamples = "0.3.4"

lazy val commonSettings =
  Seq(
    organization := "org.bitlap",
    scalaVersion := scala213,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scalatest" %% "scalatest" % "3.2.11" % Test,
    ), Compile / scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n <= 12 => Nil
        case _ => List("-Ymacro-annotations" /*, "-Ymacro-debug-verbose"*/)
      }
    } ++ Seq("-language:experimental.macros"),
    organizationName := "org.bitlap",
    startYear := Some(2022),
    licenses += ("MIT", new URL("https://github.com/bitlap/scala-macro-tools/blob/master/LICENSE")),
    Test / testOptions += Tests.Argument("-oDF"),
    Test / fork := true,
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
    ))

lazy val cacheable = (project in file("cacheable"))
  .settings(commonSettings).settings(Publishing.publishSettings)
  .settings(
    name := "smt-cacheable",
    crossScalaVersions := List(scala213, scala212),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-redis" % "0.0.0+381-86c20614-SNAPSHOT" % Provided, // 实验性质的
      "com.typesafe" % "config" % "1.4.1" % Provided,
      "dev.zio" %% "zio" % "1.0.13" % Provided,
      "dev.zio" %% "zio-schema" % "0.1.8" % Provided,
      "dev.zio" %% "zio-schema-protobuf" % "0.1.8" % Provided,
      "dev.zio" %% "zio-schema-derivation" % "0.1.8" % Test,
    ),
    excludeDependencies ++= Seq(
      InclExclRule("com.google.protobuf")
    )
  )
  .settings(paradise())
  .enablePlugins(AutomateHeaderPlugin)

lazy val core = (project in file("core"))
  .settings(commonSettings)
  .settings(
    name := "smt-core",
    crossScalaVersions := List(scala213, scala212, scala211),
    libraryDependencies ++= Seq(
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
      "com.typesafe.play" %% "play-json" % "2.7.4" % Test,
      "org.apache.logging.log4j" % "log4j-api" % "2.17.2" % Test,
      "org.apache.logging.log4j" % "log4j-core" % "2.17.2" % Test,
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.17.2" % Test,
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.2" % Test,
      "com.alipay.sofa" % "jraft-core" % "1.3.9" % Test
    ),
    ProtocConfig / sourceDirectory := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n < 13 => new File("core/src/test/resources") // test only for 2.13
        case _ => new File("core/src/test/proto")
      }
    }
  ).settings(Publishing.publishSettings)
  .settings(paradise())
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(ProtocPlugin)

lazy val root = (project in file(".")).aggregate(core, cacheable)

lazy val `scala2-13` = (project in file("examples/scala2-13")).settings(scalaVersion := scala213)
  .settings(libraryDependencies ++= Seq(
    "org.bitlap" %% "scala-macro-tools" % lastVersionForExamples,
  )).settings(
  publish / skip := true,
  Compile / scalacOptions += "-Ymacro-annotations"
)

lazy val `scala2-12` = (project in file("examples/scala2-12")).settings(scalaVersion := scala212)
  .settings(libraryDependencies ++= Seq(
    "org.bitlap" %% "scala-macro-tools" % lastVersionForExamples,
  )).settings(
  publish / skip := true,
  paradise()
)

lazy val `scala2-11` = (project in file("examples/scala2-11")).settings(scalaVersion := scala211)
  .settings(libraryDependencies ++= Seq(
    "org.bitlap" %% "scala-macro-tools" % lastVersionForExamples,
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