scalaVersion := "3.2.1"

organization := "org.mixql"

name := "mixql-protobuf-core"

description := "MixQL messages."

licenses := List(
  "Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")
)

homepage := Some(url("https://github.com/mixql/mixql-protobuf"))

pomIncludeRepository := { _ => false }

scmInfo := Some(
  ScmInfo(
    url("https://github.com/mixql/mixql-protobuf"),
    "scm:git@github.com:mixql/mixql-protobuf.git"
  )
)

val Scala212 = "2.12.17"
