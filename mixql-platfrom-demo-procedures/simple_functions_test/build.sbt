name := "mixql-simple-functions-test"
version := "0.1.0"

description := "MixQL platform demo's simple function"
scalaVersion := "3.2.1"

licenses := List(
  "Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")
)

homepage := Some(url("https://github.com/mixql/mixql-platform-demo"))
pomIncludeRepository := { _ => false }

libraryDependencies ++= {
  Seq(
    "org.scalatest" %% "scalatest" % "3.2.14" % Test,
    "org.scalameta" %% "munit"     % "0.7.29" % Test
  )
}

scmInfo := Some(
  ScmInfo(
    url("https://github.com/mixql/mixql-platform-demo"),
    "scm:git@github.com:mixql/mixql-platform-demo.git"
  )
)