lazy val root = (project in file("."))
  .dependsOn(jsaiProject)
  .settings(
    scalaVersion := "2.13.16",

    name := "scsc",

    organization := "ch.usi.l3.scsc",

    version := "0.1",

    sourcesInBase := false,

    // allow Ctrl-C to cancel tasks
    cancelable in Global := true,

    // don't run tests in parallel... nashorn parser gets confused
    Test / parallelExecution := false,
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oD"),

    resolvers += Resolver.sonatypeRepo("releases"),
    resolvers += Resolver.sonatypeRepo("snapshots"),

    // Testing
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.19",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test",
    // Kiama
    libraryDependencies += "org.bitbucket.inkytonik.kiama" %% "kiama" % "2.5.1",
    libraryDependencies += "org.bitbucket.inkytonik.kiama" %% "kiama-extras" % "2.5.1",
    // Logger
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.7",
    libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    // Shapeless
    libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.13",
    // Scalaz
    libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.3.8",
    
//     scalacOptions ++= Seq("-deprecation"),
    scalacOptions ++= Seq("-feature", "-unchecked")
  )

/*
lazy val jsaiProject =
  ProjectRef(uri("https://github.com/nystrom/jsai.git"), "jsai")
*/

lazy val jsaiProject =
  ProjectRef(file("../jsai-romanenko"), "jsai-romanenko")
