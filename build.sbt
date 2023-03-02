ThisBuild / scalaVersion := "3.2.1"

inThisBuild(
  List(
    organization := "org.mixql",
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
    credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials")
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
//    Antlr4 / javaSource := baseDirectory.value / "src" / "main" / "java" / "antlr4",
    Compile / unmanagedSourceDirectories += baseDirectory.value / "src" / "main" / "java" / "antlr4", //For stupid IDEA
    libraryDependencies ++= Seq(
      "org.antlr"                % "antlr4-runtime" % "4.8-1",
      "org.apache.logging.log4j" % "log4j-api"      % "2.19.0",
      "org.apache.logging.log4j" % "log4j-core"     % "2.19.0"
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
        "org.scalatest" %% "scalatest"     % "3.1.1" % Test,
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
        "org.scalatest" %% "scalatest"     % "3.2.14" % Test,
        "org.scala-lang" % "scala-reflect" % "2.13.8"
      )
    )
  )

lazy val mixQLProtobuf = projectMatrix
  .in(file("mixql-protobuf"))
  .dependsOn(mixQLCore)
  .settings(
    Compile / PB.targets := Seq(scalapb.gen(grpc = true) -> {
      // val file = (Compile / sourceManaged).value
      //        println("PB  target: " + file.getPath)
      //        val file = new File("target/scala-3.2.1/src_managed/main")
      val file = (Compile / scalaSource).value / "scalaPB"
      println("PB  target: " + file.getAbsolutePath)
      file
    }),
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_0.11" % "2.9.6-0" % "protobuf",
      "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_0.11" % "2.9.6-0",
      "org.scalameta" %% "munit" % "0.7.29" % Test
    )
  )
  .jvmPlatform(Seq(Scala3, Scala213, Scala212))

lazy val mixQLProtobufSCALA3 = mixQLProtobuf.jvm(Scala3)

lazy val mixQLCluster = project
  .in(file("mixql-cluster"))
  .dependsOn(mixQLProtobufSCALA3)

lazy val mixQLEngine = projectMatrix
  .in(file("mixql-engine"))
  .dependsOn(mixQLProtobuf)
  .settings(libraryDependencies ++= {
    Seq(
      "com.typesafe"            % "config"      % "1.4.2",
      "org.scalameta"          %% "munit"       % "0.7.29" % Test,
      "org.zeromq"              % "jeromq"      % "0.5.2",
      "com.github.nscala-time" %% "nscala-time" % "2.32.0"
    )
  })
  .jvmPlatform(Seq(Scala3, Scala213, Scala212))

lazy val mixQLEngineSCALA3 = mixQLEngine.jvm(Scala3)

lazy val mixQLEngineStub = project
  .in(file("engines/mixql-engine-stub"))
  .dependsOn(mixQLEngineSCALA3)
  .enablePlugins(UniversalPlugin, JavaServerAppPackaging, UniversalDeployPlugin)

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
lazy val mixQLCoreSCALA3 = mixQLCore.jvm(Scala3)
lazy val mixQLCoreSCALA212 = mixQLCore.jvm(Scala212)
lazy val mixQLCoreSCALA213 = mixQLCore.jvm(Scala213)
buildAllMixQLCore := {
//  (mixQLCluster / Compile / packageBin).value
//  (mixQLProtobuf / Compile / packageBin).value
  (mixQLCoreSCALA3 / Compile / packageBin).value
  (mixQLCoreSCALA212 / Compile / packageBin).value
  (mixQLCoreSCALA213 / Compile / packageBin).value
}
