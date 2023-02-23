import BuildHelper._

enablePlugins(ZioSbtEcosystemPlugin, ZioSbtCiPlugin)

crossScalaVersions := Seq.empty

inThisBuild(
  List(
    name := "ZIO Optics",
    ciEnabledBranches := Seq("series/2.x"),
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
    )
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

val zioVersion = "2.0.6"

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
  .settings(oldStdSettings("zio-optics"))
  .settings(oldCrossProjectSettings)
  .settings(oldBuildInfoSettings("zio.optics"))
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
  .settings(
    crossScalaVersions -= scala211.value,
    libraryDependencies += "dev.zio" %%% "zio-test-sbt" % zioVersion % Test
  )
  .settings(scalaJSUseMainModuleInitializer := true)

lazy val zioOpticsJVM = zioOptics.jvm
  .settings(scala3Settings)
  .settings(libraryDependencies += "dev.zio" %%% "zio-test-sbt" % zioVersion % Test)
  .settings(scalaReflectTestSettings)

lazy val zioOpticsNative = zioOptics.native
  .settings(nativeSettings)
  .settings(crossScalaVersions -= scala211.value)

lazy val docs = project
  .in(file("zio-optics-docs"))
  .settings(
    moduleName := "zio-optics-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    crossScalaVersions -= scala211.value,
    projectName := (ThisBuild / name).value,
    mainModuleName := (zioOpticsJVM / moduleName).value,
    projectStage := ProjectStage.Development,
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(zioOpticsJVM)
  )
  .dependsOn(zioOpticsJVM)
  .enablePlugins(WebsitePlugin)
