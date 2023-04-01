name := "mixql-engine-internal"

organizationHomepage := Some(url("https://mixql.org/"))

description := "MixQL Platform's internal engine"

run / fork := true

scalaVersion := scala3Version

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

val scala3Version = "3.2.0"


