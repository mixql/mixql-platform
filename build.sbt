ThisBuild / scalaVersion := "3.2.1"

inThisBuild(
  List(
    organization := "org.mixql",
    version := "0.5.0-SNAPSHOT", //change version for all projects
    organizationName := "MixQL",
    organizationHomepage := Some(url("https://mixql.org/")),
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
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
  )
)

val Scala3 = "3.1.3"
val Scala213 = "2.13.8"
val Scala212 = "2.12.17"

lazy val mixQLCore = projectMatrix
  .in(file("mixql-core"))
  .enablePlugins(Antlr4Plugin)
  .defaultAxes()
  .settings(
    Antlr4 / antlr4Version := "4.8-1",
    Antlr4 / antlr4GenListener := false, // default: true
    Antlr4 / antlr4GenVisitor := true, // default: true
    Antlr4 / antlr4PackageName := Some("org.mixql.core.generated"),
    Antlr4 / antlr4FolderToClean := (Antlr4 / javaSource).value / "org" / "mixql" / "core" / "generated",
    //    Antlr4 / javaSource := baseDirectory.value / "src" / "main" / "java" / "antlr4",
    Compile / unmanagedSourceDirectories += baseDirectory.value / "src" / "main" / "java" / "antlr4", // For stupid IDEA
    libraryDependencies ++= Seq(
      "org.antlr" % "antlr4-runtime" % "4.8-1",
      "org.apache.logging.log4j" % "log4j-api" % "2.19.0",
      "org.apache.logging.log4j" % "log4j-core" % "2.19.0",
      "com.typesafe" % "config" % "1.4.2"
      // "org.ow2.asm"              % "asm"        % "9.3",
      // "org.ow2.asm"              % "asm-tree"   % "9.3",
    )
  )
  .customRow(
    true,
    Seq(Scala212, Scala213),
    Seq(VirtualAxis.jvm),
    _.settings(
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "3.1.1" % Test,
        "org.scala-lang" % "scala-reflect" % scalaVersion.value
      )
    )
  )
  .customRow(
    true,
    Seq(Scala3),
    Seq(VirtualAxis.jvm),
    _.settings(
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "3.2.14" % Test,
        "org.scala-lang" % "scala-reflect" % "2.13.8"
      )
    )
  )

lazy val mixQLCoreSCALA3 = mixQLCore.jvm(Scala3)
lazy val mixQLCoreSCALA212 = mixQLCore.jvm(Scala212)
lazy val mixQLCoreSCALA213 = mixQLCore.jvm(Scala213)

lazy val mixQLRemoteMessages = projectMatrix
  .in(file("mixql-remote-messages"))
  .dependsOn(mixQLCore)
  .settings({
    val circeVersion = "0.14.1"
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion
    )
  })
  .jvmPlatform(Seq(Scala3, Scala213, Scala212))

lazy val mixQLRemoteMessagesSCALA3 = mixQLRemoteMessages.jvm(Scala3)
lazy val mixQLRemoteMessagesSCALA212 = mixQLRemoteMessages.jvm(Scala212)
lazy val mixQLRemoteMessagesSCALA213 = mixQLRemoteMessages.jvm(Scala213)

lazy val mixQLCluster = project
  .in(file("mixql-cluster"))
  .dependsOn(mixQLRemoteMessagesSCALA3)

lazy val mixQLEngine = projectMatrix
  .in(file("mixql-engine"))
  .dependsOn(mixQLRemoteMessages)
  .settings(libraryDependencies ++= {
    Seq(
      "com.typesafe" % "config" % "1.4.2",
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.zeromq" % "jeromq" % "0.5.2",
      "com.github.nscala-time" %% "nscala-time" % "2.32.0"
    )
  })
  .jvmPlatform(Seq(Scala3, Scala213, Scala212))

lazy val mixQLEngineSCALA3 = mixQLEngine.jvm(Scala3)
lazy val mixQLEngineSCALA213 = mixQLEngine.jvm(Scala213)
lazy val mixQLEngineSCALA212 = mixQLEngine.jvm(Scala212)


lazy val mixQLEngineInternal = projectMatrix
  .in(file("mixql-engine-internal"))
  .dependsOn(mixQLCore)
  .jvmPlatform(Seq(Scala3, Scala213, Scala212))

lazy val mixQLEngineInternalSCALA3 = mixQLEngineInternal.jvm(Scala3)

lazy val mixQLEngineStub = project
  .in(file("engines/mixql-engine-stub"))
  .dependsOn(mixQLEngineSCALA3)
  .enablePlugins(UniversalPlugin, JavaServerAppPackaging, UniversalDeployPlugin)

lazy val mixQLEngineStubScala213 = project
  .in(file("engines/mixql-engine-stub-scala-2-13"))
  .dependsOn(mixQLEngineSCALA213)
  .enablePlugins(UniversalPlugin, JavaServerAppPackaging, UniversalDeployPlugin)

lazy val mixQLEngineStubScala212 = project
  .in(file("engines/mixql-engine-stub-scala-2-12"))
  .dependsOn(mixQLEngineSCALA212)
  .enablePlugins(UniversalPlugin, JavaServerAppPackaging, UniversalDeployPlugin)

lazy val mixQLEngineSqlite = project
  .in(file("engines/mixql-engine-sqlite"))
  .dependsOn(mixQLEngineSCALA3)
  .enablePlugins(UniversalPlugin, JavaServerAppPackaging, UniversalDeployPlugin)

lazy val mixQLEngineSqliteScala212 = project
  .in(file("engines/mixql-engine-sqlite-scala-2-12"))
  .dependsOn(mixQLEngineSCALA212)
  .enablePlugins(UniversalPlugin, JavaServerAppPackaging, UniversalDeployPlugin)

lazy val stageEnginesDemo =
  taskKey[Seq[(File, String)]](
    "stage engines and get jars for mixqlPlatformDemo"
  )

lazy val stageEnginesOozie =
  taskKey[Seq[(File, String)]](
    "stage engines and get jars for mixqlPlatformOozie"
  )

lazy val mixQLEngineStubLocal = project
  .in(file("engines/mixql-engine-stub-local"))
  .dependsOn(mixQLEngineInternalSCALA3)

lazy val mixQLEngineSqliteLocal = project
  .in(file("engines/mixql-engine-sqlite-local"))
  .dependsOn(mixQLEngineInternalSCALA3)

///////////////////////////////////MIXQL PLATFORM DEMO FUNCTIONS/////////////////////////

lazy val mixQLPlatformDemoSimpleFuncs = project
  .in(file("mixql-platfrom-demo-procedures/simple_functions_test"))
  .dependsOn(mixQLCoreSCALA3)

/////////////////////////////////////////////////////////////////////////////////////////

lazy val mixQLPlatformDemo = project
  .in(file("mixql-platform-demo"))
  .enablePlugins(UniversalPlugin, JavaServerAppPackaging, UniversalDeployPlugin)
  .dependsOn(
    mixQLCluster,
    mixQLEngineStub % "compile->test",
    mixQLEngineSqlite % "compile->test",
    mixQLEngineStubLocal, //% "compile->compile;compile->test",
    mixQLEngineSqliteLocal, //% "compile->compile;compile->test",
    mixQLPlatformDemoSimpleFuncs, // % "compile->compile;compile->test"
  )
  .settings(stageEnginesDemo := {
    //      implicit val log = streams.value.log
    //      log.info("-------stageEnginesDemo---------")
    var cache: Seq[(File, String)] = Seq()
    (mixQLEngineStub / Universal / stage).value
    (mixQLEngineSqlite / Universal / stage).value
    (mixQLEngineSqliteScala212 / Universal / stage).value
    (mixQLEngineStubScala213 / Universal / stage).value
    (mixQLEngineStubScala212 / Universal / stage).value
    val baseDirs = Seq(
      (mixQLEngineStub / baseDirectory).value,
      (mixQLEngineSqlite / baseDirectory).value,
      (mixQLEngineSqliteScala212 / baseDirectory).value,
      (mixQLEngineStubScala213 / baseDirectory).value,
      (mixQLEngineStubScala212 / baseDirectory).value
    )

    baseDirs.foreach(baseDir => {
      cache = cache ++
        (baseDir / "target" / "universal" / "stage" / "bin")
          .listFiles()
          .toSeq
          .map(f =>
            (f, "bin/" + f.getName)
          ) ++ (baseDir / "target" / "universal" / "stage" / "lib")
        .listFiles()
        .toSeq
        .map(f => (f, "lib/" + f.getName))
    })

    cache
  })

lazy val mixQLOozie = project.in(file("mixql-oozie"))

lazy val engineClassName = settingKey[String]("Name of engine's main class")

lazy val mixQLPlatformOozie = project
  .in(file("mixql-platform-oozie"))
  .enablePlugins(UniversalPlugin, JavaServerAppPackaging, UniversalDeployPlugin)
  .dependsOn(
    mixQLCluster,
    mixQLEngineSqliteLocal, //% "compile->compile;compile->test",
    mixQLOozie
  )
  .settings(stageEnginesOozie := {
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

    //Generate shell script in engine's lib folder
    baseDirs.keys.foreach(engineName => {
      val baseDir = baseDirs(engineName)
      val jars = (baseDir / "target" / "universal" / "stage" / "lib")
        .listFiles()
        .toSeq
        .map(f => f.getName).toList

      val target = baseDir / "target" / "universal" / "stage" / "lib" / engineName
      RemoteEngineShell.gen_shell(target, engineName, engineClasses(engineName), jars)
    })

    //    Add engine's libs to cache
    baseDirs.values.foreach(baseDir => {
      cache = cache ++
        (baseDir / "target" / "universal" / "stage" / "lib")
          .listFiles()
          .toSeq
          .map(f => (f, f.getName))
    })

    cache
  },
    excludeDependencies ++= Seq(
      ExclusionRule("org.scala-lang.modules", "scala-xml_3")
    )
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

lazy val buildAllMixQLCore = taskKey[Unit]("Build all mixql core projects ")
buildAllMixQLCore := {
  //  (mixQLCluster / Compile / packageBin).value
  //  (mixQLProtobuf / Compile / packageBin).value
  (mixQLCoreSCALA3 / Compile / packageBin).value
  (mixQLCoreSCALA212 / Compile / packageBin).value
  (mixQLCoreSCALA213 / Compile / packageBin).value
}

lazy val buildAllMixQLRemoteMessages = taskKey[Unit]("Build all mixql remote messages library projects ")
buildAllMixQLRemoteMessages := {
  (mixQLRemoteMessagesSCALA3 / Compile / packageBin).value
  (mixQLRemoteMessagesSCALA213 / Compile / packageBin).value
  (mixQLRemoteMessagesSCALA212 / Compile / packageBin).value
}


lazy val archiveMixQLPlatformDemo =
  taskKey[Unit]("Create dist archive of platform-demo")
archiveMixQLPlatformDemo := Def
  .sequential(
    mixQLPlatformDemo / Universal / packageBin,
    mixQLPlatformDemo / Universal / packageZipTarball
  )
  .value

lazy val archiveMixQLPlatformOozie =
  taskKey[Unit]("Create dist archive of platform-oozie")
archiveMixQLPlatformOozie := Def
  .sequential(
    mixQLPlatformOozie / Universal / packageBin,
    mixQLPlatformOozie / Universal / packageZipTarball
  )
  .value
