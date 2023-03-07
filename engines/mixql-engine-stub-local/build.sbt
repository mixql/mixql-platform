credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials")

name := "mixql-engine-stub-local"

version := "0.1.0"

description := "MixQL stub engine internal."

scalaVersion := "3.2.1"

libraryDependencies ++= {
  Seq(
    "org.scalameta" %% "munit" % "0.7.29" % Test
  )
}

licenses := List(
  "Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")
)

homepage := Some(url("https://github.com/mixql/mixql-engine-stub"))

pomIncludeRepository := { _ => false }

publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

scmInfo := Some(
  ScmInfo(
    url("https://github.com/mixql/mixql-engine-stub"),
    "scm:git@github.com:mixql/mixql-engine-stub.git"
  )
)
