import BuildHelper._

inThisBuild(
  List(
    organization := "dev.zio",
    homepage := Some(url("https://zio.github.io/zio-optics/")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "jdegoes",
        "John De Goes",
        "john@degoes.net",
        url("http://degoes.net")
      ),
      Developer(
        "adamgfraser",
        "Adam Fraser",
        "adam.fraser@gmail.com",
        url("https://github.com/adamgfraser")
      )
    ),
    pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
    pgpPublicRing := file("/tmp/public.asc"),
    pgpSecretRing := file("/tmp/secret.asc")
  )
)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("fix", "; all compile:scalafix test:scalafix; all scalafmtSbt scalafmtAll")
addCommandAlias("check", "; scalafmtSbtCheck; scalafmtCheckAll; compile:scalafix --check; test:scalafix --check")

addCommandAlias(
  "testJVM",
  ";zioOpticsJVM/test"
)
addCommandAlias(
  "testJS",
  ";zioOpticsJS/test"
)
addCommandAlias(
  "testNative",
  ";zioOpticsNative/test:compile"
)

val zioVersion = "1.0.9"

lazy val root = project
  .in(file("."))
  .settings(
    publish / skip := true,
    unusedCompileDependenciesFilter -= moduleFilter("org.scala-js", "scalajs-library")
  )
  .aggregate(
    zioOpticsJVM,
    zioOpticsJS,
    zioOpticsNative,
    docs
  )

lazy val zioOptics = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("zio-optics"))
  .settings(stdSettings("zio-optics"))
  .settings(crossProjectSettings)
  .settings(buildInfoSettings("zio.optics"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"          % zioVersion,
      "dev.zio" %% "zio-test"     % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test
    )
  )
  .settings(testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))
  .enablePlugins(BuildInfoPlugin)

lazy val zioOpticsJS = zioOptics.js
  .settings(jsSettings)
  .settings(libraryDependencies += "dev.zio" %%% "zio-test-sbt" % zioVersion % Test)
  .settings(scalaJSUseMainModuleInitializer := true)

lazy val zioOpticsJVM = zioOptics.jvm
  .settings(dottySettings)
  .settings(libraryDependencies += "dev.zio" %%% "zio-test-sbt" % zioVersion % Test)
  .settings(scalaReflectTestSettings)

lazy val zioOpticsNative = zioOptics.native
  .settings(nativeSettings)

lazy val docs = project
  .in(file("zio-optics-docs"))
  .settings(stdSettings("zio-optics"))
  .settings(
    publish / skip := true,
    moduleName := "zio-optics-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(zioOpticsJVM),
    ScalaUnidoc / unidoc / target := (LocalRootProject / baseDirectory).value / "website" / "static" / "api",
    cleanFiles += (ScalaUnidoc / unidoc / target).value,
    docusaurusCreateSite := docusaurusCreateSite.dependsOn(Compile / unidoc).value,
    docusaurusPublishGhpages := docusaurusPublishGhpages.dependsOn(Compile / unidoc).value
  )
  .dependsOn(zioOpticsJVM)
  .enablePlugins(MdocPlugin, DocusaurusPlugin, ScalaUnidocPlugin)
