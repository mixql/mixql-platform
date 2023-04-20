import java.io.File

object PlatformOozieWorkflow {
  def genWorkflow(target: File, name: String, version: String, files: List[String]): Unit = {

    val xmlFile = xml.platform_oozie_workflow.apply(name, version, files)

    import org.apache.commons.io.FileUtils

    import java.nio.charset.StandardCharsets
    FileUtils.writeStringToFile(target,
      xmlFile.toString.replace("&quot;", "\"").replaceAll("\\r(\\n)?", "\n"),
      StandardCharsets.UTF_8)
  }
}