name := "mixql-repl"

organizationHomepage := Some(url("https://mixql.org/"))

description := "MixQL repl lib"

run / fork := true

scalaVersion := "3.2.1"

libraryDependencies ++= {
  Seq(
    "org.beryx"     % "text-io"     % "3.4.1",
    "org.beryx"     % "text-io-web" % "3.4.1",
    "com.sparkjava" % "spark-core"  % "2.9.1"
  )
}

//excludeDependencies ++= Seq(
//  ExclusionRule("com.eclipsesource.j2v8", "j2v8_win32_x86")
//)

licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/mixql/mixql-engine"))

pomIncludeRepository := { _ => false }

scmInfo := Some(ScmInfo(url("https://github.com/mixql/mixql-engine"), "scm:git@github.com:mixql/mixql-engine.git"))
