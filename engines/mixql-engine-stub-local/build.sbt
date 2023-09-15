name := "mixql-engine-stub-local"

description := "MixQL stub engine internal."

scalaVersion := "3.1.3"

libraryDependencies ++= {
  Seq("org.scalameta" %% "munit" % "0.7.29" % Test, "org.scalatest" %% "scalatest" % "3.2.14" % Test)
}

licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/mixql/mixql-engine-stub"))

pomIncludeRepository := { _ => false }

scmInfo := Some(
  ScmInfo(url("https://github.com/mixql/mixql-engine-stub"), "scm:git@github.com:mixql/mixql-engine-stub.git")
)
