package org.mixql.protobuf

import scala.util.Try

object ErrorOps {

  def stackTraceToString(e: Throwable): String = {
    import java.io.PrintWriter
    import java.io.StringWriter
    var sw: StringWriter = null
    var pw: PrintWriter = null
    try {
      sw = new StringWriter()
      pw = new PrintWriter(sw)
      e.printStackTrace(pw)
      sw.toString
    } finally {
      Try(
        if (pw != null)
          pw.close()
      )
      Try(
        if (sw != null)
          sw.close()
      )
    }
  }

}
