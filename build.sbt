import sbt.{ Def, Test }
import sbtrelease.ReleaseStateTransformations._

ThisBuild / resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("public"),
  Resolver.sonatypeRepo("snapshots"),
  "New snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots/"
)

lazy val scala212               = "2.12.14"
lazy val scala211               = "2.11.12"
lazy val scala213               = "2.13.8"
lazy val lastVersionForExamples = "0.7.3"

lazy val configVersion                = "1.4.2"
lazy val scalatestVersion             = "3.2.12"
lazy val zioVersion                   = "1.0.16"
lazy val zioLoggingVersion            = "2.0.1"
lazy val caffeineVersion              = "2.9.3"
lazy val zioRedisVersion              = "0.0.0+381-86c20614-SNAPSHOT" // 实验性质的
lazy val zioSchemaVersion             = "0.1.9"
lazy val scalaLoggingVersion          = "3.9.5"
lazy val log4jVersion                 = "2.18.0"
lazy val scalaCollectionCompatVersion = "2.8.0"

lazy val commonSettings =
  Seq(
    organization := "org.bitlap",
    startYear    := Some(2022),
    scalaVersion := scala213,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % Provided,
      "org.scala-lang" % "scala-reflect"  % scalaVersion.value % Provided,
      "org.scalatest" %% "scalatest"      % scalatestVersion   % Test
    ),
    Compile / scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n <= 12 => Nil
        case _                       => List("-Ymacro-annotations", "-Ywarn-unused" /*, "-Ymacro-debug-verbose"*/ )
      }
    } ++ Seq("-language:experimental.macros"),
    Compile / compile             := (Compile / compile).dependsOn(Compile / headerCreateAll).value,
    Global / onChangedBuildSource := ReloadOnSourceChanges,
    headerLicense                 := Some(HeaderLicense.MIT("2022", "bitlap")),
    Test / testOptions += Tests.Argument("-oDF"),
    Test / fork               := true,
    publishConfiguration      := publishConfiguration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
  )

lazy val `smt-cacheable` = (project in file("smt-cacheable"))
  .settings(commonSettings)
  .settings(Publishing.publishSettings)
  .settings(
    name               := "smt-cacheable",
    crossScalaVersions := List(scala213, scala212),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"         % zioVersion % Provided,
      "dev.zio" %% "zio-streams" % zioVersion,
      "dev.zio" %% "zio-logging" % zioLoggingVersion
    )
  )
  .settings(paradise())
  .enablePlugins(HeaderPlugin)

lazy val `smt-cacheable-caffeine` = (project in file("smt-cacheable-caffeine"))
  .settings(commonSettings)
  .settings(Publishing.publishSettings)
  .settings(
    name               := "smt-cacheable-caffeine",
    crossScalaVersions := List(scala213, scala212),
    libraryDependencies ++= Seq(
      "com.typesafe"                  % "config"   % configVersion,
      "com.github.ben-manes.caffeine" % "caffeine" % caffeineVersion
    )
  )
  .dependsOn(`smt-cacheable` % "compile->compile;test->test")
  .settings(paradise())
  .enablePlugins(HeaderPlugin)

lazy val `smt-cacheable-redis` = (project in file("smt-cacheable-redis"))
  .settings(commonSettings)
  .settings(Publishing.publishSettings)
  .settings(
    name               := "smt-cacheable-redis",
    crossScalaVersions := List(scala213, scala212),
    libraryDependencies ++= Seq(
      "dev.zio"     %% "zio-redis"             % zioRedisVersion  % Provided,
      "com.typesafe" % "config"                % configVersion,
      "dev.zio"     %% "zio-schema"            % zioSchemaVersion,
      "dev.zio"     %% "zio-schema-json"       % zioSchemaVersion,
      "dev.zio"     %% "zio-schema-derivation" % zioSchemaVersion % Test
    )
  )
  .dependsOn(`smt-cacheable` % "compile->compile;test->test")
  .settings(paradise())
  .enablePlugins(HeaderPlugin)

lazy val `smt-csv` = (project in file("smt-csv"))
  .settings(commonSettings)
  .settings(
    name               := "smt-csv",
    crossScalaVersions := List(scala213, scala212, scala211)
  )
  .dependsOn(`smt-common` % "compile->compile;test->test")
  .settings(Publishing.publishSettings)
  .settings(paradise())
  .enablePlugins(HeaderPlugin)

lazy val `smt-common` = (project in file("smt-common"))
  .settings(commonSettings)
  .settings(
    name               := "smt-common",
    crossScalaVersions := List(scala213, scala212, scala211)
  )
  .settings(Publishing.publishSettings)
  .settings(paradise())
  .enablePlugins(HeaderPlugin)

lazy val `smt-cache` = (project in file("smt-cache"))
  .settings(commonSettings)
  .settings(
    name                                            := "smt-cache",
    crossScalaVersions                              := List(scala213, scala212, scala211),
    libraryDependencies += "org.scala-lang.modules" %% "scala-collection-compat" % scalaCollectionCompatVersion
  )
  .settings(Publishing.publishSettings)
  .settings(paradise())
  .dependsOn(`smt-common` % "compile->compile;test->test")
  .enablePlugins(HeaderPlugin)

lazy val `smt-csv-derive` = (project in file("smt-csv-derive"))
  .settings(commonSettings)
  .settings(
    name               := "smt-csv-derive",
    crossScalaVersions := List(scala213, scala212, scala211)
  )
  .settings(Publishing.publishSettings)
  .settings(paradise())
  .enablePlugins(HeaderPlugin)
  .dependsOn(`smt-csv` % "compile->compile;test->test")

lazy val `smt-annotations` = (project in file("smt-annotations"))
  .settings(commonSettings)
  .settings(
    name               := "smt-annotations",
    crossScalaVersions := List(scala213, scala212, scala211),
    libraryDependencies ++= Seq(
      "com.typesafe.scala-logging" %% "scala-logging"    % scalaLoggingVersion,
      "org.apache.logging.log4j"    % "log4j-api"        % log4jVersion % Test,
      "org.apache.logging.log4j"    % "log4j-core"       % log4jVersion % Test,
      "org.apache.logging.log4j"    % "log4j-slf4j-impl" % log4jVersion % Test
    )
  )
  .settings(Publishing.publishSettings)
  .settings(paradise())
  .enablePlugins(HeaderPlugin)

lazy val `smt` = (project in file("."))
  .aggregate(
    `smt-annotations`,
    `smt-cacheable`,
    `smt-cacheable-redis`,
    `smt-cacheable-caffeine`,
    `smt-csv`,
    `smt-csv-derive`,
    `smt-cache`,
    `smt-common`,
    `scala2-11`,
    `scala2-12`,
    `scala2-13`
  )
  .settings(
    commands ++= Commands.value,
    crossScalaVersions            := Nil,
    publish / skip                := true,
    headerLicense                 := Some(HeaderLicense.MIT("2022", "bitlap")),
    releaseIgnoreUntrackedFiles   := true,
    releaseCrossBuild             := false, // @see https://www.scala-sbt.org/1.x/docs/Cross-Build.html
    releaseTagName                := (ThisBuild / version).value,
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

lazy val `scala2-13` = (project in file("examples/scala2-13"))
  .settings(scalaVersion := scala213)
  .settings(
    libraryDependencies ++= Seq(
      "org.bitlap" %% "smt-annotations"        % lastVersionForExamples,
      "org.bitlap" %% "smt-cacheable"          % lastVersionForExamples,
      "org.bitlap" %% "smt-cacheable-redis"    % lastVersionForExamples,
      "org.bitlap" %% "smt-cacheable-caffeine" % lastVersionForExamples,
      "dev.zio"    %% "zio-redis"              % zioRedisVersion,
      "dev.zio"    %% "zio"                    % zioVersion
    )
  )
  .settings(
    publish / skip := true,
    Compile / scalacOptions ++= List("-Ymacro-annotations", "-Ywarn-unused")
  )

lazy val `scala2-12` = (project in file("examples/scala2-12"))
  .settings(scalaVersion := scala212)
  .settings(
    libraryDependencies ++= Seq(
      "org.bitlap" %% "smt-annotations"        % lastVersionForExamples,
      "org.bitlap" %% "smt-cacheable"          % lastVersionForExamples,
      "org.bitlap" %% "smt-cacheable-redis"    % lastVersionForExamples,
      "org.bitlap" %% "smt-cacheable-caffeine" % lastVersionForExamples,
      "dev.zio"    %% "zio-redis"              % zioRedisVersion,
      "dev.zio"    %% "zio"                    % zioVersion
    )
  )
  .settings(
    publish / skip := true,
    scalacOptions ++= List("-Xlint:unused"),
    paradise()
  )

lazy val `scala2-11` = (project in file("examples/scala2-11"))
  .settings(scalaVersion := scala211)
  .settings(
    libraryDependencies ++= Seq(
      "org.bitlap" %% "smt-annotations" % lastVersionForExamples
    )
  )
  .settings(
    publish / skip := true,
    scalacOptions ++= List("-Xlint:unused"),
    paradise()
  )

def paradise(): Def.Setting[Seq[ModuleID]] =
  libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n < 13 => Some("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
    case _                      => None
  }).fold(Seq.empty[ModuleID])(f => Seq(compilerPlugin(f)))

ThisBuild / logLevel := Level.Warn
