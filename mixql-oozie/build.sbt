name := "mixql-oozie"

organizationHomepage := Some(url("https://mixql.org/"))

description := "MixQL oozie lib"

run / fork := true

scalaVersion := "3.2.1"

libraryDependencies ++= {
  val vOozieClient = "5.2.0"
  Seq("org.apache.oozie" % "oozie-client" % vOozieClient % Provided)
}

excludeDependencies ++= Seq(ExclusionRule("com.eclipsesource.j2v8", "j2v8_win32_x86"))

licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/mixql/mixql-engine"))

pomIncludeRepository := { _ => false }

scmInfo := Some(ScmInfo(url("https://github.com/mixql/mixql-engine"), "scm:git@github.com:mixql/mixql-engine.git"))
