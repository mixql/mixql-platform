import java.io.File

object RemoteEngineShell {

  def gen_shell(target: File, ENGINE_NAME: String, ENGINE_CLASS: String, JARS: List[String]) = {
    val shFile = txt.remote_engine_shell.apply(ENGINE_NAME, ENGINE_CLASS, JARS)

    import org.apache.commons.io.FileUtils

    import java.nio.charset.StandardCharsets
    FileUtils.writeStringToFile(target, shFile.toString.replaceAll("\\r(\\n)?", "\n"), StandardCharsets.UTF_8)
  }
}
