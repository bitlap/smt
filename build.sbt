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
lazy val lastVersionForExamples = "0.4.0-SNAPSHOT"

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
    Compile / compile := (Compile / compile).dependsOn(Compile / headerCreateAll).value,
    organizationName := "org.bitlap",
    startYear := Some(2022),
    headerLicense := Some(HeaderLicense.MIT("2022", "bitlap")),
    licenses += License.MIT,
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

lazy val `cacheable-core` = (project in file("cacheable-core"))
  .settings(commonSettings).settings(Publishing.publishSettings)
  .settings(
    name := "smt-cacheable-core",
    crossScalaVersions := List(scala213, scala212),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "1.0.13",
      "dev.zio" %% "zio-streams" % "1.0.13",
      "dev.zio" %% "zio-logging" % "0.5.14"
    ),
    excludeDependencies ++= Seq(
      InclExclRule("com.google.protobuf")
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
      "com.typesafe" % "config" % "1.4.2",
      "com.github.ben-manes.caffeine" % "caffeine" % "2.9.3"
    ),
    excludeDependencies ++= Seq(
      InclExclRule("com.google.protobuf")
    )
  ).dependsOn(`cacheable-core`)
  .settings(paradise())
  .enablePlugins(HeaderPlugin)

lazy val `cacheable-redis` = (project in file("cacheable-redis"))
  .settings(commonSettings).settings(Publishing.publishSettings)
  .settings(
    name := "smt-cacheable-redis",
    crossScalaVersions := List(scala213, scala212),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-redis" % "0.0.0+381-86c20614-SNAPSHOT", // 实验性质的
      "com.typesafe" % "config" % "1.4.2",
      "dev.zio" %% "zio-schema" % "0.1.8",
      "dev.zio" %% "zio-schema-protobuf" % "0.1.8",
      "dev.zio" %% "zio-schema-derivation" % "0.1.8" % Test,
    ),
    excludeDependencies ++= Seq(
      InclExclRule("com.google.protobuf")
    )
  ).dependsOn(`cacheable-core`)
  .settings(paradise())
  .enablePlugins(HeaderPlugin)

lazy val `cacheable-benchmark` = (project in file("cacheable-benchmark"))
  .settings(commonSettings)
  .settings(
    name := "smt-cacheable-benchmark",
    publish / skip := true,
    excludeDependencies ++= Seq(
      InclExclRule("com.google.protobuf")
    )
  ).dependsOn(`cacheable-core`, `cacheable-redis`, `cacheable-caffeine`)
  .settings(paradise())
  .enablePlugins(HeaderPlugin, JmhPlugin)

lazy val tools = (project in file("tools"))
  .settings(commonSettings)
  .settings(
    name := "smt-tools",
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
        case Some((2, n)) if n < 13 => new File("tools/src/test/resources") // test only for 2.13
        case _ => new File("tools/src/test/proto")
      }
    }
  ).settings(Publishing.publishSettings)
  .settings(paradise())
  .enablePlugins(HeaderPlugin, ProtocPlugin)

lazy val root = (project in file(".")).aggregate(tools, `cacheable-core`, `cacheable-redis`, `cacheable-caffeine`, `cacheable-benchmark`)
  .settings(
    publishArtifact := false,
    publish / skip := true,
    headerLicense := Some(HeaderLicense.MIT("2022", "bitlap"))
  )

lazy val `scala2-13` = (project in file("examples/scala2-13")).settings(scalaVersion := scala213)
  .settings(libraryDependencies ++= Seq(
    "org.bitlap" %% "smt-tools" % lastVersionForExamples,
    "org.bitlap" %% "smt-cacheable-core" % lastVersionForExamples,
    "org.bitlap" %% "smt-cacheable-redis" % lastVersionForExamples,
    "org.bitlap" %% "smt-cacheable-caffeine" % lastVersionForExamples,
  )).settings(
  publish / skip := true,
  excludeDependencies ++= Seq(
    InclExclRule("com.google.protobuf")
  ),
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
  excludeDependencies ++= Seq(
    InclExclRule("com.google.protobuf")
  ),
  paradise()
)

lazy val `scala2-11` = (project in file("examples/scala2-11")).settings(scalaVersion := scala211)
  .settings(libraryDependencies ++= Seq(
    "org.bitlap" %% "smt-tools" % lastVersionForExamples,
  )).settings(
  excludeDependencies ++= Seq(
    InclExclRule("com.google.protobuf")
  ),
  publish / skip := true,
  paradise()
)

def paradise(): Def.Setting[Seq[ModuleID]] = {
  libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n < 13 => Some("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
    case _ => None
  }).fold(Seq.empty[ModuleID])(f => Seq(compilerPlugin(f)))
}