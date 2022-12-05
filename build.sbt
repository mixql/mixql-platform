val scala3Version = "3.2.1"

lazy val root = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "mixql-engine-demo",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= {
      val vScallop = "4.1.0"
      Seq(
        "org.rogach" %% "scallop" % vScallop,
        "com.typesafe" % "config" % "1.4.2",
        "org.scalameta" %% "munit" % "0.7.29" % Test,
        "org.zeromq" % "jeromq" % "0.5.2",
        "com.github.nscala-time" %% "nscala-time" % "2.32.0",
        "org.mixql" %% "mixql-core" % "0.1.0-SNAPSHOT",
        "org.mixql" %% "mixql-protobuf" % "0.1.0-SNAPSHOT",
        "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_0.11" % "2.9.6-0",
      )
    }
  )

lazy val stageAll = taskKey[Unit]("Stage all projects")
lazy val packArchive = taskKey[Unit]("Making release tar.gz")
lazy val makeTarGZ = taskKey[Unit]("Pack target dist tar.gz")

val projects_stage = ScopeFilter(inProjects(root), inConfigurations(Universal))

stageAll := {
  stage.all(projects_stage).value
}

packArchive := Def.sequential(stageAll, makeTarGZ).value

makeTarGZ := {
  import sbt.internal.util.ManagedLogger
  implicit val log = streams.value.log
  //TO-DO
}
