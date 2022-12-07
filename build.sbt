import com.typesafe.sbt.packager.SettingsHelper.{
  addPackage,
  makeDeploymentSettings
}

lazy val root = project
  .in(file("."))
  .enablePlugins(UniversalPlugin, JavaServerAppPackaging, UniversalDeployPlugin)
  .settings(
    credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials"),
    organization := "org.mixql",
    name := "mixql-engine-stub",
    organizationHomepage := Some(url("https://mixql.org/")),
    description := "MixQL stub engine.",
    scalaVersion := "3.2.1",
    resolvers +=
      "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
    libraryDependencies ++= {
      val vScallop = "4.1.0"
      Seq(
        "org.rogach"    %% "scallop"      % vScallop,
        "com.typesafe"   % "config"       % "1.4.2",
        "org.mixql"     %% "mixql-engine" % "0.1.0-SNAPSHOT",
        "org.scalameta" %% "munit"        % "0.7.29" % Test
      )
    },
    licenses := List(
      "Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")
    ),
    homepage := Some(url("https://github.com/mixql/mixql-engine-stub")),
    pomIncludeRepository := { _ => false },
    publishTo := {
      val nexus = "https://s01.oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    Universal / packageZipTarball / mappings += file(
      "README.md"
    ) -> "README.md",
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/mixql/mixql-engine-stub"),
        "scm:git@github.com:mixql/mixql-engine-stub.git"
      )
    ),
    developers := List(
      Developer(
        "LavrVV",
        "MixQL team",
        "lavr3x@rambler.ru",
        url("https://github.com/LavrVV")
      ),
      Developer(
        "wiikviz",
        "Kostya Kviz",
        "kviz@outlook.com",
        url("https://github.com/wiikviz")
      ),
      Developer(
        "mihan1235",
        "MixQL team",
        "mihan1235@yandex.ru",
        url("https://github.com/mihan1235")
      ),
      Developer(
        "ntlegion",
        "MixQL team",
        "ntlegion@outlook.com",
        url("https://github.com/ntlegion")
      )
    )
  )

// zip
makeDeploymentSettings(Universal, packageBin in Universal, "zip")

makeDeploymentSettings(UniversalDocs, packageBin in UniversalDocs, "zip")

// additional tgz
addPackage(Universal, packageZipTarball in Universal, "tgz")

// additional txz
addPackage(UniversalDocs, packageXzTarball in UniversalDocs, "txz")
