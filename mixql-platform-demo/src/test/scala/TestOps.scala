import java.io.File

object TestOps {

  def getFileFromResource(path: String): Option[File] = {
    import java.net.URL
    val resource = getClass.getClassLoader.getResource(path)
    if (resource == null) None
    else
      Some(new File(resource.getFile))
  }
}
