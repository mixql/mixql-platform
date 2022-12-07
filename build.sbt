lazy val root = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials"),
    name := "mixql-engine-stub",
    organizationHomepage := Some(url("https://mixql.org/")),
    description := "MixQL stub engine.",
    scalaVersion := scala3Version,
    resolvers +=
      "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
    libraryDependencies ++= {
      val vScallop = "4.1.0"
      Seq(
        "org.rogach"    %% "scallop"      % vScallop,
        "com.typesafe"   % "config"       % "1.4.2",
        "org.mixql"     %% "mixql-engine" % "0.1.0-SNAPSHOT",
        "org.scalameta" %% "munit"        % "0.7.29" % Test
      )
    },
    licenses := List(
      "Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")
    ),
    homepage := Some(url("https://github.com/mixql/mixql-engine-stub")),
    pomIncludeRepository := { _ => false },
    publishTo := {
      val nexus = "https://s01.oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/mixql/mixql-engine-stub"),
        "scm:git@github.com:mixql/mixql-engine-stub.git"
      )
    ),
    developers := List(
      Developer(
        "LavrVV",
        "MixQL team",
        "lavr3x@rambler.ru",
        url("https://github.com/LavrVV")
      ),
      Developer(
        "wiikviz",
        "Kostya Kviz",
        "kviz@outlook.com",
        url("https://github.com/wiikviz")
      ),
      Developer(
        "mihan1235",
        "MixQL team",
        "mihan1235@yandex.ru",
        url("https://github.com/mihan1235")
      ),
      Developer(
        "ntlegion",
        "MixQL team",
        "ntlegion@outlook.com",
        url("https://github.com/ntlegion")
      )
    )
  )
lazy val stageAll = taskKey[Unit]("Stage all projects")
lazy val packArchive = taskKey[Unit]("Making release tar.gz")
lazy val makeTarGZ = taskKey[Unit]("Pack target dist tar.gz")
lazy val scala3Version = "3.2.1"
lazy val projects_stage =
  ScopeFilter(inProjects(root), inConfigurations(Universal))

stageAll := {
  stage.all(projects_stage).value
}

packArchive := Def.sequential(stageAll, makeTarGZ).value

makeTarGZ := {
  implicit val log = streams.value.log

  IO.delete(new File(s"target/${name.value}-${version.value}.tar.gz"))

  log.info(s"Pack ${(root / target).value / s"${name.value}-${version.value}"}")

  TarGzArchiver.createTarGz(
    new File(s"target/${name.value}-${version.value}.tar.gz"),
    s"${name.value}-${version.value}/",
    new File(s"target/universal/stage/bin"),
    new File(s"target/universal/stage/lib")
  )
  log.info("Task `packArchive` completed successfully")
}
