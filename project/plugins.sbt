val zioSbtVersion = "0.3.10+43-080b598b-SNAPSHOT"

addSbtPlugin(
  "dev.zio"                     % "zio-sbt-ecosystem" % zioSbtVersion exclude ("org.scala-js", "sbt-scalajs") exclude ("org.scala-native", "sbt-scala-native")
)
addSbtPlugin(
  "dev.zio"                     % "zio-sbt-website"   % zioSbtVersion exclude ("org.scala-js", "sbt-scalajs") exclude ("org.scala-native", "sbt-scala-native")
)
addSbtPlugin(
  "dev.zio"                     % "zio-sbt-ci"        % zioSbtVersion exclude ("org.scala-js", "sbt-scalajs") exclude ("org.scala-native", "sbt-scala-native")
)
addSbtPlugin("com.typesafe"     % "sbt-mima-plugin"   % "1.1.1")
addSbtPlugin("org.scala-js"     % "sbt-scalajs"       % "1.12.0") // Still supports 2.11
addSbtPlugin("org.scala-native" % "sbt-scala-native"  % "0.4.9")  // Still supports 2.11

resolvers ++= Resolver.sonatypeOssRepos("public")

import sbt.internal.librarymanagement.mavenint.PomExtraDependencyAttributes

ThisBuild / dependencyOverrides ++= List(
  "org.scala-js"     % "sbt-scalajs"      % "1.12.0",
  "org.scala-native" % "sbt-scala-native" % "0.4.9"
).map(
  _.extra(
    PomExtraDependencyAttributes.SbtVersionKey   -> (update / scalaBinaryVersion).value,
    PomExtraDependencyAttributes.ScalaVersionKey -> sbtVersion.value
  )
)
