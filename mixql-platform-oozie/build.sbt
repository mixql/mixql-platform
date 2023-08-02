import com.typesafe.sbt.packager.SettingsHelper.{addPackage, makeDeploymentSettings}

name := "mixql-platform-oozie"

description := "MixQL platform oozie, that can run MixQl engines, executed by oozie"
scalaVersion := "3.3.0"

licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/mixql/mixql-platform-demo"))
pomIncludeRepository := { _ => false }

Universal / mappings := {
  val applicationConf = baseDirectory.value / "src" / "main" / "resources" / "reference.conf"
  val log4jConf = baseDirectory.value / "src" / "main" / "resources" / "log4j2.xml"

  val platformOozieFiles =
    ((Universal / mappings).value :+
      file("README.md") -> "README.md" :+
      applicationConf -> "application.conf" :+
      log4jConf -> s"${log4jConf.getName}").map(t => t._1 -> t._2.replaceFirst("lib/", "")).distinct.filter(t =>
      if (t._2.startsWith("bin/"))
        false
      else
        true
    )

  var allFiles = platformOozieFiles ++ prePackArchive.value

  /// Generate shell///
  {
    val target = baseDirectory.value / "target" / "universal" / "stage" / "bin" / name.value
    val platformClassName = "org.mixql.platform.oozie.MixQlEnginePlatformOozie"
    PlatformOozieShell.gen_shell(target, platformClassName, platformOozieFiles.map(t => t._2).distinct.toList)
    allFiles = allFiles :+ target -> name.value
  }

  /// Generate workflow///
  {
    val target = baseDirectory.value / "target" / "universal" / "stage" / "workflow.xml"
    val platformClassName = "org.mixql.platform.oozie.MixQlEnginePlatformOozie"
    PlatformOozieWorkflow.genWorkflow(target, name.value, version.value, allFiles.map(t => t._2).distinct.toList)
    allFiles = allFiles :+ target -> "workflow.xml"
  }

  ///////// Delete duplicates of folders, because of buf in zip archive format
  import scala.collection.mutable
  val targetDirs: mutable.Set[String] = mutable.Set()
  var listSeq: List[(File, String)] = List()
  allFiles.foreach(t => {
    if (!targetDirs.contains(t._2)) {
      targetDirs.add(t._2)
      listSeq = listSeq :+ t
    }
  })
  listSeq
}

libraryDependencies ++= {
  val vScallop = "4.1.0"
  val vOozieClient = "5.2.0"
  Seq(
    "org.rogach"      %% "scallop"      % vScallop,
    "com.typesafe"     % "config"       % "1.4.2",
    "org.scalatest"   %% "scalatest"    % "3.2.14"     % Test,
    "org.scalameta"   %% "munit"        % "0.7.29"     % Test,
    "org.apache.oozie" % "oozie-client" % vOozieClient % Provided
  )
}

excludeDependencies ++= Seq(ExclusionRule("com.eclipsesource.j2v8", "j2v8_win32_x86"))

scmInfo := Some(
  ScmInfo(url("https://github.com/mixql/mixql-platform-demo"), "scm:git@github.com:mixql/mixql-platform-demo.git")
)

// zip
makeDeploymentSettings(Universal, packageBin in Universal, "zip")

makeDeploymentSettings(UniversalDocs, packageBin in UniversalDocs, "zip")

// additional tgz
addPackage(Universal, packageZipTarball in Universal, "tgz")

// additional txz
addPackage(UniversalDocs, packageXzTarball in UniversalDocs, "txz")

lazy val prePackArchive = taskKey[Seq[(File, String)]]("action before making release tar.gz")

//val projects_stage = ScopeFilter(inProjects(root), inConfigurations(Universal))

import com.typesafe.config.{Config, ConfigFactory}

val configMixqlPlatform = settingKey[Config]("config of mixql platform demo")
configMixqlPlatform := ConfigFactory.parseFile(baseDirectory.value / "mixql_platform.conf")

lazy val stageEnginesOozie = taskKey[Seq[(File, String)]]("stage engines and get jars for mixqlPlatformOozie")

prePackArchive := {
  implicit val log = streams.value.log

  import scala.util.{Try, Failure, Success}
  var cache: Seq[(File, String)] = stageEnginesOozie.value

  cache =
    cache ++ {
      Try {
        configMixqlPlatform.value.getStringList("org.mixql.engines")
      } match {
        case Failure(exception) =>
          log.warn(
            "Error while getting list of engine uris: " +
              exception.getMessage
          )
          Seq()
        case Success(uris) =>
          import scala.collection.JavaConverters._

          uris.asScala.foreach(uri => {
            val (name, version) = parseUri(uri)
            downloadAndExtractModule(name, version, uri, new File(s"target/$name-$version.tar.gz"))
            cache =
              cache ++
                (baseDirectory.value / "target" / s"$name-$version" / "bin").listFiles().toSeq
                  .map(f => (f, f.getName)) ++ (baseDirectory.value / "target" / s"$name-$version" / "lib").listFiles()
                  .toSeq.map(f => (f, f.getName))
          })
          cache
      }
    }
  cache
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
  if (name.endsWith("-"))
    name = name.dropRight(1)
  val version = """\d+\.\d+\.\d+(-SNAPSHOT)?""".r.findFirstIn(endPart).get
  (name, version)
}
