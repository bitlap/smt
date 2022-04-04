import sbt.librarymanagement.InclExclRule
import sbt.{ Def, Test }
import sbtrelease.ReleaseStateTransformations._

ThisBuild / resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("public"),
  Resolver.sonatypeRepo("snapshots"),
  "New snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots/"
)

lazy val scala212 = "2.12.14"
lazy val scala211 = "2.11.12"
lazy val scala213 = "2.13.8"
lazy val lastVersionForExamples = "0.4.2"


lazy val scalatestVersion = "3.2.11"
lazy val zioVersion = "1.0.13"
lazy val zioLoggingVersion = "0.5.14"
lazy val configVersion = "1.4.2"
lazy val caffeineVersion = "2.9.3"
lazy val zioRedisVersion = "0.0.0+381-86c20614-SNAPSHOT" // 实验性质的
lazy val zioSchemaVersion = "0.1.9"
lazy val scalaLoggingVersion = "3.9.4"
lazy val playJsonVersion = "2.7.4"
lazy val log4jVersion = "2.17.2"
lazy val jacksonScalaVersion = "2.13.2"
lazy val jraftVersion = "1.3.9"
lazy val protocVersion = "3.19.4"

lazy val commonSettings =
  Seq(
    organization := "org.bitlap",
    organizationName := "bitlap",
    startYear := Some(2022),
    scalaVersion := scala213,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scalatest" %% "scalatest" % scalatestVersion % Test,
    ), Compile / scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n <= 12 => Nil
        case _ => List("-Ymacro-annotations" /*, "-Ymacro-debug-verbose"*/)
      }
    } ++ Seq("-language:experimental.macros"),
    Compile / compile := (Compile / compile).dependsOn(Compile / headerCreateAll).value,
    Global / onChangedBuildSource := ReloadOnSourceChanges,
    headerLicense := Some(HeaderLicense.MIT("2022", "bitlap")),
    Test / testOptions += Tests.Argument("-oDF"),
    Test / fork := true,
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
  )

lazy val `cacheable-core` = (project in file("cacheable-core"))
  .settings(commonSettings).settings(Publishing.publishSettings)
  .settings(
    name := "smt-cacheable-core",
    crossScalaVersions := List(scala213, scala212),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion, // FIXME we should use compile or provide ???
      "dev.zio" %% "zio-streams" % zioVersion,
      "dev.zio" %% "zio-logging" % zioLoggingVersion
    )
  )
  .settings(paradise())
  .enablePlugins(HeaderPlugin)

lazy val `cacheable-caffeine` = (project in file("cacheable-caffeine"))
  .settings(commonSettings).settings(Publishing.publishSettings)
  .settings(
    name := "smt-cacheable-caffeine",
    crossScalaVersions := List(scala213, scala212),
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % configVersion,
      "com.github.ben-manes.caffeine" % "caffeine" % caffeineVersion
    )
  ).dependsOn(`cacheable-core` % "compile->compile;test->test")
  .settings(paradise())
  .enablePlugins(HeaderPlugin)

lazy val `cacheable-redis` = (project in file("cacheable-redis"))
  .settings(commonSettings).settings(Publishing.publishSettings)
  .settings(
    name := "smt-cacheable-redis",
    crossScalaVersions := List(scala213, scala212),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-redis" % zioRedisVersion,
      "com.typesafe" % "config" % configVersion,
      "dev.zio" %% "zio-schema" % zioSchemaVersion,
      "dev.zio" %% "zio-schema-json" % zioSchemaVersion,
      "dev.zio" %% "zio-schema-derivation" % zioSchemaVersion % Test,
    )
  ).dependsOn(`cacheable-core` % "compile->compile;test->test")
  .settings(paradise())
  .enablePlugins(HeaderPlugin)

lazy val `cacheable-benchmark` = (project in file("cacheable-benchmark"))
  .settings(commonSettings)
  .settings(
    name := "smt-cacheable-benchmark",
    publish / skip := true
  ).dependsOn(`cacheable-core`, `cacheable-redis`, `cacheable-caffeine`)
  .settings(paradise())
  .enablePlugins(HeaderPlugin, JmhPlugin)

lazy val tools = (project in file("tools"))
  .settings(commonSettings)
  .settings(
    name := "smt-tools",
    crossScalaVersions := List(scala213, scala212, scala211),
    libraryDependencies ++= Seq(
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
      "com.typesafe.play" %% "play-json" % playJsonVersion % Test,
      "org.apache.logging.log4j" % "log4j-api" % log4jVersion % Test,
      "org.apache.logging.log4j" % "log4j-core" % log4jVersion % Test,
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion % Test,
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonScalaVersion % Test,
      "com.alipay.sofa" % "jraft-core" % jraftVersion % Test,
      "com.google.protobuf" % "protobuf-java" % protocVersion % Test
    )
  ).settings(Publishing.publishSettings)
  .settings(paradise())
  .enablePlugins(HeaderPlugin)

lazy val root = (project in file(".")).aggregate(tools, `cacheable-core`, `cacheable-redis`, `cacheable-caffeine`, `cacheable-benchmark`)
  .settings(
    crossScalaVersions := Nil,
    publish / skip := true,
    headerLicense := Some(HeaderLicense.MIT("2022", "bitlap")),
    releaseIgnoreUntrackedFiles := true,
    releaseCrossBuild := false, //@see https://www.scala-sbt.org/1.x/docs/Cross-Build.html
    releaseTagName := (ThisBuild / version).value,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
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
  )

//FIXME  root doesnot aggregate these examples
lazy val `scala2-13` = (project in file("examples/scala2-13")).settings(scalaVersion := scala213)
  .settings(libraryDependencies ++= Seq(
    "org.bitlap" %% "smt-tools" % lastVersionForExamples,
    "org.bitlap" %% "smt-cacheable-core" % lastVersionForExamples,
    "org.bitlap" %% "smt-cacheable-redis" % lastVersionForExamples,
    "org.bitlap" %% "smt-cacheable-caffeine" % lastVersionForExamples,
  )).settings(
  publish / skip := true,
  Compile / scalacOptions += "-Ymacro-annotations"
)

lazy val `scala2-12` = (project in file("examples/scala2-12")).settings(scalaVersion := scala212)
  .settings(libraryDependencies ++= Seq(
    "org.bitlap" %% "smt-tools" % lastVersionForExamples,
    "org.bitlap" %% "smt-cacheable-core" % lastVersionForExamples,
    "org.bitlap" %% "smt-cacheable-redis" % lastVersionForExamples,
    "org.bitlap" %% "smt-cacheable-caffeine" % lastVersionForExamples
  )).settings(
  publish / skip := true,
  paradise()
)

lazy val `scala2-11` = (project in file("examples/scala2-11")).settings(scalaVersion := scala211)
  .settings(libraryDependencies ++= Seq(
    "org.bitlap" %% "smt-tools" % lastVersionForExamples,
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