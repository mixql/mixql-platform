//Use if sbt-antlr4 plugin with snapshot version is used
resolvers ++=
  Seq(
    "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
    "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/releases"
  )

addSbtPlugin("com.thesamet"        % "sbt-protoc"          % "1.0.6")
addSbtPlugin("org.scalameta"       % "sbt-scalafmt"        % "2.4.6")
addSbtPlugin("org.xerial.sbt"      % "sbt-sonatype"        % "3.9.15")
addSbtPlugin("com.github.sbt"      % "sbt-pgp"             % "2.1.2")
addSbtPlugin("org.jetbrains.scala" % "sbt-ide-settings"    % "1.1.1")
addSbtPlugin("org.scoverage"       % "sbt-scoverage"       % "2.0.6")
addSbtPlugin("com.github.sbt"      % "sbt-native-packager" % "1.9.11")
addSbtPlugin("org.mixql"           % "sbt-antlr4"       % "0.8.7-SNAPSHOT")
addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.9.0")

libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-compress" % "1.21",
  "commons-io" % "commons-io" % "2.11.0",
  "com.typesafe" % "config" % "1.4.2",
  "com.thesamet.scalapb" %% "compilerplugin" % "0.11.10"
)
dependencyOverrides += "org.scala-lang.modules" % "scala-xml_2.12" % "2.1.0"

enablePlugins(SbtTwirl)