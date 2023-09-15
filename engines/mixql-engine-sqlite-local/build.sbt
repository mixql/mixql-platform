updateOptions in ThisBuild := updateOptions.value.withGigahorse(false)

name := "mixql-engine-sqlite-local"
description := "MixQL sqlite internal engine."
scalaVersion := "3.1.3"

libraryDependencies ++= {
  Seq(
    "org.scalameta" %% "munit"       % "0.7.29" % Test,
    "org.xerial"     % "sqlite-jdbc" % "3.40.0.0",
    "org.scalatest" %% "scalatest"   % "3.2.14" % Test
  )
}

licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/mixql/mixql-engine-stub"))

pomIncludeRepository := { _ => false }

scmInfo := Some(
  ScmInfo(url("https://github.com/mixql/mixql-engine-stub"), "scm:git@github.com:mixql/mixql-engine-stub.git")
)
