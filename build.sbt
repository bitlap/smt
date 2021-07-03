import sbtrelease.ReleaseStateTransformations._

name := "scala-macro-tools"
scalaVersion := "2.13.6"
organization := "io.github.jxnu-liguobin"

lazy val scala212 = "2.12.13"
lazy val scala211 = "2.11.12"
lazy val scala213 = "2.13.6"
lazy val supportedScalaVersions = List(scala213, scala212, scala211)

lazy val root = (project in file("."))
  .settings(
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "com.typesafe.play" %% "play-json" % "2.7.4" % Test,
      "org.scalatest" %% "scalatest" % "3.0.9" % Test
    ), Compile / scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n <= 12 => Nil
        case _ => List("-Ymacro-annotations" /*, "-Ymacro-debug-verbose"*/)
      }
    },
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
  ).settings(Publishing.publishSettings).settings(paradise).aggregate(`intellij-plugin`)

lazy val `examples213` = (project in file("examples213")).settings(scalaVersion := scala213)
  .settings(libraryDependencies ++= Seq(
    "io.github.jxnu-liguobin" %% "scala-macro-tools" % (version in ThisBuild).value,
  )).settings(
  publish / skip := true,
  Compile / scalacOptions += "-Ymacro-annotations"
)

lazy val `examples212` = (project in file("examples212")).settings(scalaVersion := scala212)
  .settings(libraryDependencies ++= Seq(
    "io.github.jxnu-liguobin" %% "scala-macro-tools" % (version in ThisBuild).value,
  )).settings(
  publish / skip := true,
  paradise
)

val paradise = {
  libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n < 13 => Some("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
    case _ => None
  }).fold(Seq.empty[ModuleID])(f => Seq(compilerPlugin(f)))
}

// Only to import, and every thing in /intellij-plugin.
lazy val `intellij-plugin` = project in file("intellij-plugin")

