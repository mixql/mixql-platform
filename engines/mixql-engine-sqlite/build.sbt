import com.typesafe.sbt.packager.SettingsHelper.{
  addPackage,
  makeDeploymentSettings
}

updateOptions in ThisBuild := updateOptions.value.withGigahorse(false)

name := "mixql-engine-sqlite"
description := "MixQL stub engine."
scalaVersion := "3.2.1"

libraryDependencies ++= {
  val vScallop = "4.1.0"
  Seq(
    "org.rogach"    %% "scallop"      % vScallop,
    "org.scalameta" %% "munit"        % "0.7.29" % Test,
    "org.xerial"     % "sqlite-jdbc"  % "3.40.0.0",
    "org.scalatest" %% "scalatest"    % "3.2.14" % Test
  )
}

licenses := List(
  "Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")
)

homepage := Some(url("https://github.com/mixql/mixql-engine-stub"))

pomIncludeRepository := { _ => false }

Universal / mappings += file("README.md") -> "README.md"

scmInfo := Some(
  ScmInfo(
    url("https://github.com/mixql/mixql-engine-stub"),
    "scm:git@github.com:mixql/mixql-engine-stub.git"
  )
)

// zip
makeDeploymentSettings(Universal, packageBin in Universal, "zip")

makeDeploymentSettings(UniversalDocs, packageBin in UniversalDocs, "zip")

// additional tgz
addPackage(Universal, packageZipTarball in Universal, "tgz")

// additional txz
addPackage(UniversalDocs, packageXzTarball in UniversalDocs, "txz")
