import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype.autoImport.sonatypeProfileName

/**
 * sbt publish setting
 *
 * @author 梦境迷离 dreamylost
 * @since 2020-07-19
 * @version v1.0
 */
object Publishing {

  //publish by sbt publishSigned
  lazy val publishSettings = Seq(
    credentials += Credentials(Path.userHome / ".ivy2" / ".sonatype_credentials"),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    publishMavenStyle := true,
    Test / publishArtifact := false,
    pomIncludeRepository := { _ => false },
    developers := List(
      Developer(
        id = "dreamylost",
        name = "梦境迷离",
        email = "dreamylost@outlook.com",
        url = url("https://dreamylost.cn")
      ),
      Developer(
        id = "IceMimosa",
        name = "ChenKai",
        email = "chk19940609@gmail.com",
        url = url("http://patamon.me")
      )
    ),
    sonatypeProfileName := organization.value,
    isSnapshot := version.value endsWith "SNAPSHOT",
    homepage := Some(url("https://bitlap.org")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/bitlap/scala-macro-tools"),
        "scm:git@github.com:bitlap/scala-macro-tools.git"
      ))
  )
}
