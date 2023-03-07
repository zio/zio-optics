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
    ),
    supportedScalaVersions :=
      Map(
        (zioOptics.jvm / thisProject).value.id    -> (zioOptics.jvm / crossScalaVersions).value,
        (zioOptics.native / thisProject).value.id -> (zioOptics.native / crossScalaVersions).value,
        (zioOptics.js / thisProject).value.id     -> (zioOptics.js / crossScalaVersions).value
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

val zioVersion = "2.0.10"

lazy val root = project
  .in(file("."))
  .settings(
    publish / skip := true,
    unusedCompileDependenciesFilter -= moduleFilter("org.scala-js", "scalajs-library")
  )
  .aggregate(
    zioOptics.jvm,
    zioOptics.js,
    zioOptics.native,
    docs
  )

lazy val zioOptics = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("zio-optics"))
  .settings(stdSettings(name = "zio-optics", packageName = Some("zio.optics"), enableCrossProject = true))
  .settings(enableZIO())
  .settings(
    libraryDependencies += "dev.zio" %%% "zio-test-sbt" % zioVersion % Test,
    excludeDependencies ++= List(
      ExclusionRule("org.portable-scala", "portable-scala-reflect_2.13")
    )
  )
  .jvmSettings(scala3Settings)
  .jvmSettings(scalaReflectTestSettings)
  .jsSettings(jsSettings)
  .jsSettings(scalaJSUseMainModuleInitializer := true)
  .nativeSettings(nativeSettings)

lazy val docs = project
  .in(file("zio-optics-docs"))
  .settings(
    moduleName := "zio-optics-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    crossScalaVersions -= scala211.value,
    projectName := (ThisBuild / name).value,
    mainModuleName := (zioOptics.jvm / moduleName).value,
    projectStage := ProjectStage.Development,
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(zioOptics.jvm)
  )
  .dependsOn(zioOptics.jvm)
  .enablePlugins(WebsitePlugin)
