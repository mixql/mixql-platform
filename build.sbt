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
        "org.mixql" %% "mixql-engine-core" % "0.1.0-SNAPSHOT"
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

  IO.delete(new File(s"target/${name.value}-${version.value}.tar.gz"))

  log.info(s"Pack ${(root / target).value / s"${name.value}-${version.value}"}")

  TarGzArchiver.createTarGz(new File(s"target/${name.value}-${version.value}.tar.gz"),
    name.value + "/",
    new File(s"target/universal/stage/bin"),
    new File(s"target/universal/stage/lib")
  )
  log.info("Task `packArchive` completed successfully")
}

