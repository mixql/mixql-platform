import java.io.File

object PlatformOozieShell {
  def gen_shell(target: File, PLATFORM_OOZIE_CLASS: String, JARS: List[String]) = {
    val shFile = txt.platform_oozie_shell.apply(PLATFORM_OOZIE_CLASS, JARS)

    import org.apache.commons.io.FileUtils

    import java.nio.charset.StandardCharsets
    FileUtils.writeStringToFile(target, shFile.toString.replaceAll("\\r(\\n)?", "\n"), StandardCharsets.UTF_8)
  }
}
