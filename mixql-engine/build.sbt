lazy val root = project
  .in(file("."))
  .settings(
    credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials"),
    organization := "org.mixql",
    name := "mixql-engine",
    version := "0.1.0",
    organizationName := "MixQL",
    organizationHomepage := Some(url("https://mixql.org/")),
    description := "MixQL engine interface.",
    crossScalaVersions := ScalaVersions,
    ThisBuild / scalaVersion := Scala212,
    resolvers ++=
      Seq(
        "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
        "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/releases"
      ),
    libraryDependencies ++= {
      Seq(
        "com.typesafe"            % "config"         % "1.4.2",
        "org.scalameta"          %% "munit"          % "0.7.29" % Test,
        "org.zeromq"              % "jeromq"         % "0.5.2",
        "com.github.nscala-time" %% "nscala-time"    % "2.32.0",
        "org.mixql"              %% "mixql-protobuf" % "0.1.0"
      )
    },
    scalacOptions := {
      val stdOptions = Seq("-feature", "-deprecation")
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 13)) => stdOptions
        case Some((2, 12)) => stdOptions
        case Some((3, _)) =>
          stdOptions ++
            Seq(
              ///////////////////////////////////////////////////////////////////////////////////
              // https://docs.scala-lang.org/scala3/guides/migration/tooling-syntax-rewriting.html
              //      "-new-syntax", "-rewrite",
              //      "-indent", "-rewrite",
              ///////////////////////////////////////////////////////////////////////////////////
              //      "-source",
              //      "3.0-migration",
              "-Xmax-inlines:139", // https://github.com/lampefl/dotty/issues/13044
              "-Xmax-inlined-trees:12000000" // https://github.com/lampefl/dotty/issues/13044
            )
      }
    },
    licenses := List(
      "Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")
    ),
    homepage := Some(url("https://github.com/mixql/mixql-engine")),
    pomIncludeRepository := { _ => false },
    publishTo := {
      val nexus = "https://s01.oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/mixql/mixql-engine"),
        "scm:git@github.com:mixql/mixql-engine.git"
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

val Scala3 = "3.2.1"
val Scala213 = "2.13.8"
val Scala212 = "2.12.17"

val ScalaVersions = Seq(Scala212, Scala213, Scala3)
