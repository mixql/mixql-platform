import com.typesafe.sbt.packager.SettingsHelper.{
  addPackage,
  makeDeploymentSettings
}

lazy val root = project
  .in(file("."))
  .enablePlugins(UniversalPlugin, JavaServerAppPackaging, UniversalDeployPlugin)
  .settings(
    name := "mixql-platform-demo",
    version := "0.1.0",

    credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials"),
    organization := "org.mixql",
    organizationHomepage := Some(url("https://mixql.org/")),
    description := "MixQL platform demo, that can run and test remote MixQl engines locally",
    scalaVersion := "3.2.1",
    resolvers +=
      "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
    licenses := List(
      "Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")
    ),
    homepage := Some(url("https://github.com/mixql/mixql-platform-demo")),
    pomIncludeRepository := { _ => false },
    publishTo := {
      val nexus = "https://s01.oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    Universal / mappings := {
      val origSeq = ((Universal / mappings).value ++
        prePackArchive.value :+ file(
        "README.md"
      ) -> "README.md")

      import scala.collection.mutable
      val targetDirs: mutable.Set[String] = mutable.Set()
      var listSeq: List[(File,String)] = List()
      origSeq.foreach(
        t => {
          if (!targetDirs.contains(t._2)) {
            targetDirs.add(t._2)
            listSeq = listSeq :+ t
          }
        }
      )
      listSeq.toSeq
    },
    libraryDependencies ++= {
      val vScallop = "4.1.0"
      Seq(
        "org.rogach" %% "scallop" % vScallop,
        "com.typesafe" % "config" % "1.4.2",
        "org.mixql" %% "mixql-cluster" % "0.1.0",
        "org.mixql" %% "mixql-engine-stub" % "0.1.0" % Test,
        "org.scalatest" %% "scalatest" % "3.2.14" % Test,
        "org.scalameta" %% "munit" % "0.7.29" % Test,
        "org.mixql" %% "mixql-engine-sqlite" % "0.1.0" % Test
      )
    },
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/mixql/mixql-platform-demo"),
        "scm:git@github.com:mixql/mixql-platform-demo.git"
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

lazy val prePackArchive = taskKey[Seq[(File, String)]]("action before making release tar.gz")

val projects_stage = ScopeFilter(inProjects(root), inConfigurations(Universal))

import com.typesafe.config.{Config, ConfigFactory}

val configMixqlPlatform = ConfigFactory.parseFile(new File("./mixql_platform.conf"))

prePackArchive := {
  implicit val log = streams.value.log

  import scala.util.{Try, Failure, Success}

  Try {
    configMixqlPlatform.getStringList("org.mixql.engines")
  } match {
    case Failure(exception) => log.error("Error while getting list of engine uris: " +
      exception.getMessage
    )
      Seq()
    case Success(uris) =>
      import scala.collection.JavaConverters._
      var cache: Seq[(File, String)] = Seq()
      uris.asScala.foreach(
        uri => {
          val (name, version) = parseUri(uri)
          downloadAndExtractModule(name, version, uri, new File(s"target/$name-$version.tar.gz"))
          cache = cache ++
            ((root / baseDirectory).value / "target" / s"$name-$version" / "bin").listFiles().toSeq.map(
              f => (f, "bin/" + f.getName)
            ) ++ ((root / baseDirectory).value / "target" / s"$name-$version" / "lib").listFiles().toSeq.map(
            f => (f, "lib/" + f.getName)
          )
        }
      )
      cache
  }
}

def downloadAndExtractModule(name: String, version: String, uri: String, localTarGzFile: File): Unit = {
  if (version.endsWith("-SNAPSHOT")) {
    IO.delete(localTarGzFile)
    IO.delete(new File(s"target/$name-$version"))
  }

  if (!localTarGzFile.exists()) {
    FileDownloader.downloadFile(uri, localTarGzFile)
    IO.delete(new File(s"target/$name-$version"))
    TarGzArchiver.extractTarGz(localTarGzFile, new File(s"target/"))
  } else {
    if (!new File(s"target/$name-$version").exists()) {
      TarGzArchiver.extractTarGz(localTarGzFile, new File(s"target/"))
    }
  }
}

def parseUri(uri: String): (String, String) = {
  //  if (!uri.endsWith(".tar.gz"))
  //    throw new Exception("Uri must be for downloading tar gz archive. Example: " +
  //      "http://127.0.0.1:8080/org/mixql/engine-demo/mixql-engine-demo-0.1.0-SNAPSHOT.tar.gz"
  //    )
  val endPart = uri.split("""/""").last
  var name = """[A-Za-z\-]+""".r.findFirstIn(endPart).get
  if (name.endsWith("-")) name = name.dropRight(1)
  val version = """\d+\.\d+\.\d+(-SNAPSHOT)?""".r.findFirstIn(endPart).get
  (name, version)
}
