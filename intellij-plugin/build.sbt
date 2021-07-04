name := "scala-macro-tools"
scalaVersion := "2.13.6"
organization := "io.github.jxnu-liguobin"

lazy val scala213 = "2.13.6"

import org.jetbrains.sbtidea.Keys._

lazy val `intellij-plugin` = (project in file("."))
  .enablePlugins(SbtIdeaPlugin)
  .settings(
    version := (version in ThisBuild).value,
    scalaVersion := scala213,
    ThisBuild / intellijPluginName := "Scala-Macro-Tools",
    ThisBuild / intellijBuild := "211.7628.21", // @see https://confluence.jetbrains.com/display/IDEADEV/IDEA+2021.1+latest+builds
    ThisBuild / intellijPlatform := IntelliJPlatform.IdeaCommunity,
    Global / intellijAttachSources := true,
    Compile / javacOptions ++= "--release" :: "11" :: Nil,
    Global / scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xfatal-warnings",),
    intellijPlugins ++= Seq("com.intellij.java", "com.intellij.java-i18n"/*, "org.intellij.scala"*/).map(_.toPlugin),
    libraryDependencies ++= Seq(
      "com.eclipsesource.minimal-json" % "minimal-json" % "0.9.5" withSources(),
    ),
    unmanagedResourceDirectories in Compile += baseDirectory.value / "src" / "main" / "resources",
    unmanagedResourceDirectories in Test += baseDirectory.value / "src" / "test" / "resources",
    publish / skip := true,
  )
