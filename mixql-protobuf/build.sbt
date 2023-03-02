scalaVersion := "3.2.1"

organization := "org.mixql"

name := "mixql-protobuf"

version := "0.1.0"

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

publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}



val Scala3 = "3.2.1"
val Scala213 = "2.13.8"
val Scala212 = "2.12.17"
