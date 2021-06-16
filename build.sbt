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
      "org.scalatest" %% "scalatest" % "3.0.9" % Test
    ), Compile / scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n <= 12 => Nil
        case _ => List("-Ymacro-annotations" /*, "-Ymacro-debug-verbose"*/)
      }
    },
    releaseIgnoreUntrackedFiles := true,
    releaseCrossBuild := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      releaseStepCommandAndRemaining("^ compile"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("^ publishSigned"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  ).settings(Publishing.publishSettings).settings(
  libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) => if (n <= 12) Option("org.scalamacros" % s"paradise_$scala212" % "2.1.1") else None
    case _ => None
  }).fold(Seq.empty[ModuleID])(f => Seq(compilerPlugin(f)))
)


lazy val `examples213` = (project in file("examples213")).settings(scalaVersion := scala213)
  .settings(libraryDependencies ++= Seq(
    "io.github.jxnu-liguobin" %% "scala-macro-tools" % (version in ThisBuild).value,
  )).settings(Compile / scalacOptions += "-Ymacro-annotations")

lazy val `examples212` = (project in file("examples212")).settings(scalaVersion := scala212)
  .settings(libraryDependencies ++= Seq(
    "io.github.jxnu-liguobin" %% "scala-macro-tools" % (version in ThisBuild).value,
  )).settings(
  libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) => if (n <= 12) Option("org.scalamacros" % s"paradise_$scala212" % "2.1.1") else None
    case _ => None
  }).fold(Seq.empty[ModuleID])(f => Seq(compilerPlugin(f))))

