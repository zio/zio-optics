import sbt._
import sbt.Keys._
import sbtbuildinfo._
import BuildInfoKeys._

object BuildHelper {
  private val versions: Map[String, String] = {
    import org.snakeyaml.engine.v2.api.{Load, LoadSettings}

    import java.util.{List => JList, Map => JMap}
    import scala.jdk.CollectionConverters._

    val doc = new Load(LoadSettings.builder().build())
      .loadFromReader(scala.io.Source.fromFile(".github/workflows/ci.yml").bufferedReader())
    val yaml = doc.asInstanceOf[JMap[String, JMap[String, JMap[String, JMap[String, JMap[String, JList[String]]]]]]]
    val list = yaml.get("jobs").get("test").get("strategy").get("matrix").get("scala").asScala
    list.map(v => (v.split('.').take(2).mkString("."), v)).toMap
  }

  private val Scala212 = versions("2.12")
  private val Scala213 = versions("2.13")

  private val SilencerVersion = "1.7.5"

  private val stdOptions = Seq(
    "-encoding",
    "UTF-8",
    "-explaintypes",
    "-Yrangepos",
    "-feature",
    "-language:higherKinds",
    "-language:existentials",
    "-Xlint:_,-type-parameter-shadow",
    "-Xsource:2.13",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-unchecked",
    "-deprecation",
    "-Xfatal-warnings"
  )

  private val stdOpts213 = Seq(
    "-Wunused:imports",
    "-Wvalue-discard",
    "-Wunused:patvars",
    "-Wunused:privates",
    "-Wunused:params",
    "-Wvalue-discard"
  )

  private val stdOptsUpto212 = Seq(
    "-Xfuture",
    "-Ypartial-unification",
    "-Ywarn-nullary-override",
    "-Yno-adapted-args",
    "-Ywarn-infer-any",
    "-Ywarn-inaccessible",
    "-Ywarn-nullary-unit",
    "-Ywarn-unused-import"
  )

  private def extraOptions(scalaVersion: String) =
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 13)) =>
        stdOpts213
      case Some((2, 12)) =>
        Seq(
          "-opt-warnings",
          "-Ywarn-extra-implicit",
          "-Ywarn-unused:_,imports",
          "-Ywarn-unused:imports",
          "-opt:l:inline",
          "-opt-inline-from:<source>"
        ) ++ stdOptsUpto212
      case _ =>
        Seq("-Xexperimental") ++ stdOptsUpto212
    }

  def buildInfoSettings(packageName: String) =
    Seq(
      buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, isSnapshot),
      buildInfoPackage := packageName,
      buildInfoObject := "BuildInfo"
    )

  def stdSettings(prjName: String) = Seq(
    name := s"$prjName",
    crossScalaVersions := Seq(Scala212, Scala213),
    ThisBuild / scalaVersion := Scala212,
    scalacOptions := stdOptions ++ extraOptions(scalaVersion.value),
    libraryDependencies ++=
      Seq(
        ("com.github.ghik" % "silencer-lib" % SilencerVersion % Provided)
          .cross(CrossVersion.full),
        compilerPlugin(("com.github.ghik" % "silencer-plugin" % SilencerVersion).cross(CrossVersion.full)),
        compilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")
      ),
    incOptions ~= (_.withLogRecompileOnMacro(false))
  )
}
