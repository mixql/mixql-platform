addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.6")

libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-compress" % "1.21",
  "commons-io" % "commons-io" % "2.11.0"
)