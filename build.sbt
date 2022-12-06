val Scala3 = "3.2.1"
val Scala213 = "2.13.8"
val Scala212 = "2.12.17"
val ScalaVersions = Seq(Scala212, Scala213, Scala3)

ThisBuild / scalaVersion := Scala212

inThisBuild(
  List(
    organization := "org.mixql",
    version := "0.1.0-SNAPSHOT",
    homepage := Some(url("https://github.com/mixql/mixql-protobuf.git")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "LavrVV",
        "mixql team",
        "lavr3x@rambler.ru",
        sbt.url("http://mixql.org/")
      ),
      Developer(
        "wiikviz ",
        "mixql team",
        "kviz@outlook.com",
        sbt.url("http://mixql.org/")
      ),
      Developer(
        "mihan1235",
        "mixql team",
        "mihan1235@yandex.ru",
        sbt.url("http://mixql.org/")
      )
    )
  )
)

lazy val root = project
  .in(file("."))
  .settings(
    name := "mixql-engine-core",
    version := "0.1.0-SNAPSHOT",

    crossScalaVersions := ScalaVersions,

    libraryDependencies ++= {
      Seq(
        "com.typesafe" % "config" % "1.4.2",
        "org.scalameta" %% "munit" % "0.7.29" % Test,
        "org.zeromq" % "jeromq" % "0.5.2",
        "com.github.nscala-time" %% "nscala-time" % "2.32.0",
        "org.mixql" %% "mixql-protobuf" % "0.1.0-SNAPSHOT"
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
    }
  )
