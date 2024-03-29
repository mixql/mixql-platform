ThisBuild / scalaVersion := "3.1.3"

inThisBuild(
  List(
    organization := "org.mixql",
    version := "0.9.3", // change version for all projects
    organizationName := "MixQL",
    organizationHomepage := Some(url("https://mixql.org/")),
    developers := List(
      Developer("LavrVV", "MixQL team", "lavr3x@rambler.ru", url("https://github.com/LavrVV")),
      Developer("wiikviz", "Kostya Kviz", "kviz@outlook.com", url("https://github.com/wiikviz")),
      Developer("mihan1235", "MixQL team", "mihan1235@yandex.ru", url("https://github.com/mihan1235")),
      Developer("ntlegion", "MixQL team", "ntlegion@outlook.com", url("https://github.com/ntlegion"))
    ),
    resolvers ++=
      Seq(
        "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
        "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/releases"
      ),
    credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials"),
    publishTo := {
      val nexus = "https://s01.oss.sonatype.org/"
      if (version.value.endsWith("-SNAPSHOT") || version.value.endsWith("-snapshot"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "content/repositories/releases/")
    }
  )
)

val Scala3 = "3.1.3"
val Scala213 = "2.13.12"
val Scala212 = "2.12.17"

lazy val mixQLCore = projectMatrix.in(file("mixql-core")).enablePlugins(Antlr4Plugin).defaultAxes().settings(
  Antlr4 / antlr4Version := "4.8-1",
  Antlr4 / antlr4GenListener := false, // default: true
  Antlr4 / antlr4GenVisitor := true, // default: true
  Antlr4 / antlr4PackageName := Some("org.mixql.core.generated"),
  Antlr4 / antlr4FolderToClean := (Antlr4 / javaSource).value / "org" / "mixql" / "core" / "generated",
  //    Antlr4 / javaSource := baseDirectory.value / "src" / "main" / "java" / "antlr4",
  Compile / unmanagedSourceDirectories += baseDirectory.value / "src" / "main" / "java" / "antlr4", // For stupid IDEA
  libraryDependencies ++= Seq(
    "org.antlr"                % "antlr4-runtime" % "4.8-1",
    "org.apache.logging.log4j" % "log4j-api"      % "2.19.0",
    "org.apache.logging.log4j" % "log4j-core"     % "2.19.0",
    "com.typesafe"             % "config"         % "1.4.2"
    // "org.ow2.asm"              % "asm"        % "9.3",
    // "org.ow2.asm"              % "asm-tree"   % "9.3",
  )
).customRow(
  true,
  Seq(Scala212, Scala213),
  Seq(VirtualAxis.jvm),
  _.settings(libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "3.1.1" % Test))
).customRow(
  true,
  Seq(Scala3),
  Seq(VirtualAxis.jvm),
  _.settings(libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "3.2.14" % Test))
)

lazy val mixQLCoreSCALA3 = mixQLCore.jvm(Scala3)
lazy val mixQLCoreSCALA212 = mixQLCore.jvm(Scala212)
lazy val mixQLCoreSCALA213 = mixQLCore.jvm(Scala213)

lazy val mixQLEngine = projectMatrix.in(file("mixql-engine")).dependsOn(mixQLCore).settings(libraryDependencies ++= {
  Seq(
    "com.typesafe"               % "config"      % "1.4.2",
    "org.scalameta"             %% "munit"       % "0.7.29"   % Test,
    "org.zeromq"                 % "jeromq"      % "0.5.3",
    "com.github.nscala-time"    %% "nscala-time" % "2.32.0",
    "com.googlecode.json-simple" % "json-simple" % "1.1.1",
    "org.json"                   % "json"        % "20230227" % Test
  )
}).jvmPlatform(Seq(Scala3, Scala213, Scala212))

lazy val mixQLEngineSCALA3 = mixQLEngine.jvm(Scala3)
  .dependsOn(mixQLCoreSCALA3 % "compile->compile;compile->test;test->test;")
lazy val mixQLEngineSCALA213 = mixQLEngine.jvm(Scala213)
lazy val mixQLEngineSCALA212 = mixQLEngine.jvm(Scala212)

lazy val mixQLCluster = project.in(file("mixql-cluster")).dependsOn(mixQLEngineSCALA3)

lazy val mixQLEngineStub = project.in(file("engines/mixql-engine-stub")).dependsOn(mixQLEngineSCALA3)
  .enablePlugins(UniversalPlugin, JavaServerAppPackaging, UniversalDeployPlugin)

lazy val mixQLEngineSqlite = project.in(file("engines/mixql-engine-sqlite"))
  .dependsOn(mixQLEngineSCALA3 % "compile->compile;compile->test;test->test;")
  .enablePlugins(UniversalPlugin, JavaServerAppPackaging, UniversalDeployPlugin)

lazy val stageEnginesDemo = taskKey[Seq[(File, String)]]("stage engines and get jars for mixqlPlatformDemo")

lazy val stageEnginesOozie = taskKey[Seq[(File, String)]]("stage engines and get jars for mixqlPlatformOozie")

lazy val mixQLEngineStubLocal = project.in(file("engines/mixql-engine-stub-local"))
  .dependsOn(mixQLEngineSCALA3 % "compile->compile;compile->test;test->test;")

lazy val mixQLEngineSqliteLocal = project.in(file("engines/mixql-engine-sqlite-local"))
  .dependsOn(mixQLEngineSCALA3) //, mixQLCoreSCALA3 % "compile->compile;compile->test")

lazy val mixQLPlatformDemo = project.in(file("mixql-platform-demo"))
  .enablePlugins(UniversalPlugin, JavaServerAppPackaging, UniversalDeployPlugin).dependsOn(
    mixQLRepl,
    mixQLCluster,
    mixQLEngineStub   % "compile->test",
    mixQLEngineSqlite % "compile->test",
    mixQLEngineStubLocal, // % "compile->compile;compile->test",
    mixQLEngineSqliteLocal // % "compile->compile;compile->test",
  ).settings(
    stageEnginesDemo := {
      //      implicit val log = streams.value.log
      //      log.info("-------stageEnginesDemo---------")
      var cache: Seq[(File, String)] = Seq()
      (mixQLEngineStub / Universal / stage).value
      (mixQLEngineSqlite / Universal / stage).value
      val baseDirs = Seq((mixQLEngineStub / baseDirectory).value, (mixQLEngineSqlite / baseDirectory).value)

      baseDirs.foreach(baseDir => {
        cache =
          cache ++
            (baseDir / "target" / "universal" / "stage" / "bin").listFiles().toSeq
              .map(f => (f, "bin/" + f.getName)) ++ (baseDir / "target" / "universal" / "stage" / "lib").listFiles()
              .toSeq.map(f => (f, "lib/" + f.getName))
      })

      cache
    },
    Test / parallelExecution := false
  )

lazy val mixQLOozie = project.in(file("mixql-oozie"))

lazy val engineClassName = settingKey[String]("Name of engine's main class")

lazy val mixQLRepl = project.in(file("mixql-repl")).dependsOn(mixQLEngineSCALA3, mixQLCluster)

lazy val mixQLPlatformOozie = project.in(file("mixql-platform-oozie"))
  .enablePlugins(UniversalPlugin, JavaServerAppPackaging, UniversalDeployPlugin).dependsOn(
    mixQLRepl,
    mixQLCluster,
    mixQLEngineSqliteLocal, // % "compile->compile;compile->test",
    mixQLOozie
  ).settings(
    stageEnginesOozie := {
      (mixQLEngineSqlite / Universal / stage).value
      val baseDirs: Map[String, File] = Map(
        (mixQLEngineSqlite / name).value -> (mixQLEngineSqlite / baseDirectory).value
      )

      val engineClasses: Map[String, String] = Map(
        (mixQLEngineSqlite / name).value -> (mixQLEngineSqlite / engineClassName).value
      )

      //      implicit val log = streams.value.log
      //      log.info("-------stageEnginesDemo---------")
      var cache: Seq[(File, String)] = Seq()

      // Generate shell script in engine's lib folder
      baseDirs.keys.foreach(engineName => {
        val baseDir = baseDirs(engineName)
        val jars = (baseDir / "target" / "universal" / "stage" / "lib").listFiles().toSeq.map(f => f.getName).toList

        val target = baseDir / "target" / "universal" / "stage" / "lib" / engineName
        RemoteEngineShell.gen_shell(target, engineName, engineClasses(engineName), jars)
      })

      //    Add engine's libs to cache
      baseDirs.values.foreach(baseDir => {
        cache =
          cache ++
            (baseDir / "target" / "universal" / "stage" / "lib").listFiles().toSeq.map(f => (f, f.getName))
      })

      cache
    },
    excludeDependencies ++= Seq(ExclusionRule("org.scala-lang.modules", "scala-xml_3"))
  )

//
//lazy val cleanAll = taskKey[Unit]("Stage all projects")
//
//cleanAll := {
//  (mixQLCluster / clean).value
//  (mixQLProtobuf / clean).value
//  (mixQLCore / clean).value
//}
//

lazy val buildAllMixQLCore = taskKey[Unit]("Build all mixql core projects")

buildAllMixQLCore := {
  //  (mixQLCluster / Compile / packageBin).value
  //  (mixQLProtobuf / Compile / packageBin).value
  (mixQLCoreSCALA3 / Compile / packageBin).value
  (mixQLCoreSCALA212 / Compile / packageBin).value
  (mixQLCoreSCALA213 / Compile / packageBin).value
}

lazy val archiveMixQLPlatformDemo = taskKey[Unit]("Create dist archive of platform-demo")

archiveMixQLPlatformDemo := Def
  .sequential(mixQLPlatformDemo / Universal / packageBin, mixQLPlatformDemo / Universal / packageZipTarball).value

lazy val archiveMixQLPlatformOozie = taskKey[Unit]("Create dist archive of platform-oozie")

archiveMixQLPlatformOozie := Def
  .sequential(mixQLPlatformOozie / Universal / packageBin, mixQLPlatformOozie / Universal / packageZipTarball).value

Test / test := Def.sequential(
//  test in Test,
//  test.all(ScopeFilter(projectsTest, inConfigurations(Test)))
  mixQLPlatformDemo / Test / test,
  mixQLPlatformOozie / Test / test,
  mixQLOozie / Test / test,
  mixQLRepl / Test / test,
  mixQLEngineSqliteLocal / Test / test,
  mixQLEngineStubLocal / Test / test,
  mixQLEngineSqlite / Test / test,
  mixQLEngineStub / Test / test,
  mixQLCluster / Test / test,
  mixQLEngineSCALA3 / Test / test,
  mixQLCoreSCALA3 / Test / test
).value
/////////////////////////////////For github actions tests//////////////////////////////////////////////////////////////

lazy val testGitHubActions = taskKey[Unit]("subset of tests for github action without sockets, as the hang")

testGitHubActions := Def.sequential(
  (mixQLPlatformDemo / Test / testOnly).toTask(" TestBooleanExpressions"),
  (mixQLPlatformDemo / Test / testOnly).toTask(" TestParsingUri"),
  (mixQLPlatformDemo / Test / testOnly).toTask(" TestSimpleQueries"),
  (mixQLPlatformDemo / Test / testOnly).toTask(" TestSqlCreateTableFor"),
  (mixQLPlatformDemo / Test / testOnly).toTask(" TestSqlLightSakila"),
  (mixQLPlatformDemo / Test / testOnly).toTask(" TestSqlLightTitanic"),
  (mixQLPlatformDemo / Test / testOnly).toTask(" TestStubLocalEngineSimpleFuncs"),
  (mixQLPlatformDemo / Test / testOnly).toTask(" TestSimpleFuncs"),
  mixQLOozie / Test / test,
  mixQLRepl / Test / test,
  mixQLEngineSqliteLocal / Test / test,
  mixQLEngineStubLocal / Test / test,
  mixQLEngineSqlite / Test / test,
  mixQLEngineStub / Test / test,
  mixQLCluster / Test / test,
  mixQLEngineSCALA3 / Test / test,
  mixQLCoreSCALA3 / Test / test
).value

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

ThisBuild / Test / parallelExecution := false
ThisBuild / Test / fork := true
ThisBuild / libraryDependencies ++= Seq("org.xerial" % "sqlite-jdbc" % "3.40.0.0" % Test)

lazy val format = taskKey[Unit]("format src, test, sbt")

val projectsFormat = inProjects(
  mixQLPlatformDemo,
  mixQLPlatformOozie,
  mixQLOozie,
  mixQLRepl,
  mixQLCoreSCALA3,
  mixQLEngineSqliteLocal,
  mixQLEngineStubLocal,
  mixQLEngineSqlite,
  mixQLEngineStub,
  mixQLCluster,
  mixQLEngineSCALA3
)

format := Def.sequential(
  scalafmtAll.all(ScopeFilter(projectsFormat, inConfigurations(Test, Compile))),
  scalafmtSbt.all(ScopeFilter(projectsFormat, inConfigurations(Compile))),
  scalafmtAll,
  Compile / scalafmtSbt
).value
