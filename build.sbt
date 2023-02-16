import sbt.{ Def, Test }

ThisBuild / resolvers ++= Seq(
  Resolver.mavenLocal,
  "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype OSS Releases" at "https://s01.oss.sonatype.org/content/repositories/releases"
)

inThisBuild(
  List(
    organization           := "org.bitlap",
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository     := "https://s01.oss.sonatype.org/service/local",
    homepage               := Some(url("https://github.com/bitlap/smt")),
    licenses               := Seq(License.MIT),
    developers := List(
      Developer(
        id = "dreamylost",
        name = "梦境迷离",
        email = "dreamylost@outlook.com",
        url = url("https://blog.dreamylost.cn")
      ),
      Developer(
        id = "IceMimosa",
        name = "ChenKai",
        email = "chk19940609@gmail.com",
        url = url("http://patamon.me")
      )
    )
  )
)

lazy val scala212 = "2.12.17"
lazy val scala211 = "2.11.12"
lazy val scala213 = "2.13.8"

lazy val scalatestVersion             = "3.2.15"
lazy val scalaLoggingVersion          = "3.9.5"
lazy val log4jVersion                 = "2.19.0"
lazy val scalaCollectionCompatVersion = "2.9.0"
lazy val h2                           = "2.1.214"

lazy val commonSettings =
  Seq(
    organization := "org.bitlap",
    startYear    := Some(2023),
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

lazy val `smt-csv` = (project in file("smt-csv"))
  .settings(commonSettings)
  .settings(
    name               := "smt-csv",
    crossScalaVersions := List(scala213, scala212, scala211)
  )
  .dependsOn(`smt-common` % "compile->compile;test->test")
  .settings(paradise())
  .enablePlugins(HeaderPlugin)

lazy val `smt-common` = (project in file("smt-common"))
  .settings(commonSettings)
  .settings(
    name               := "smt-common",
    crossScalaVersions := List(scala213, scala212, scala211),
    libraryDependencies += (
      "com.h2database" % "h2" % h2 % Test
    )
  )
  .settings(paradise())
  .enablePlugins(HeaderPlugin)

lazy val `smt-cache` = (project in file("smt-cache"))
  .settings(commonSettings)
  .settings(
    name                                            := "smt-cache",
    crossScalaVersions                              := List(scala213, scala212, scala211),
    libraryDependencies += "org.scala-lang.modules" %% "scala-collection-compat" % scalaCollectionCompatVersion
  )
  .settings(paradise())
  .dependsOn(`smt-common` % "compile->compile;test->test")
  .enablePlugins(HeaderPlugin)

lazy val `smt-csv-derive` = (project in file("smt-csv-derive"))
  .settings(commonSettings)
  .settings(
    name               := "smt-csv-derive",
    crossScalaVersions := List(scala213, scala212, scala211)
  )
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
  .settings(paradise())
  .enablePlugins(HeaderPlugin)

lazy val `smt` = (project in file("."))
  .aggregate(
    `smt-annotations`,
    `smt-csv`,
    `smt-csv-derive`,
    `smt-cache`,
    `smt-common`
  )
  .settings(
    commands ++= Commands.value,
    crossScalaVersions := Nil,
    publish / skip     := true,
    headerLicense      := Some(HeaderLicense.MIT("2022", "bitlap"))
  )

def paradise(): Def.Setting[Seq[ModuleID]] =
  libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n < 13 => Some("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
    case _                      => None
  }).fold(Seq.empty[ModuleID])(f => Seq(compilerPlugin(f)))

ThisBuild / logLevel := Level.Warn
