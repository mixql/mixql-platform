import sbt.Keys.{scalaVersion, scalacOptions}
import sbt.librarymanagement.CrossVersion

object ScalaCompilerSettings {
  val stdOptions =
    Seq("-feature", "-deprecation")

  def stdSettings =
    Seq(scalacOptions := extraOptions(scalaVersion.value))

  def extraOptions(scalaVersion: String): Seq[String] = {
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 13)) => stdOptions
      case Some((2, 12)) => stdOptions
      case Some((3, _)) =>
        Seq(
          "-feature",
          "-deprecation",
          ///////////////////////////////////////////////////////////////////////////////////
          // https://docs.scala-lang.org/scala3/guides/migration/tooling-syntax-rewriting.html
          //      "-new-syntax", "-rewrite",
          //      "-indent", "-rewrite",
          ///////////////////////////////////////////////////////////////////////////////////
          //      "-source",
          //      "3.0-migration",
          "-Xmax-inlines:139", // https://github.com/lampefl/dotty/issues/13044
          "-Xmax-inlined-trees:12000000" // https://github.com/lampefl/dotty/issues/13044
        )
    }
  }
}
