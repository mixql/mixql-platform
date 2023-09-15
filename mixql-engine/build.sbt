organization := "org.mixql"

name := "mixql-engine"

description := "MixQL engine interface."

scalaVersion := Scala212

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

licenses := List(
  "Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")
)

homepage := Some(url("https://github.com/mixql/mixql-engine"))

pomIncludeRepository := { _ => false }

scmInfo := Some(
  ScmInfo(
    url("https://github.com/mixql/mixql-engine"),
    "scm:git@github.com:mixql/mixql-engine.git"
  )
)

val Scala3 = "3.1.3"
val Scala213 = "2.13.8"
val Scala212 = "2.12.17"
