package org.mixql.oozie

object OozieParamsReader {
  private var _reader: Option[OozieParamsReader] = None

  def getAllOozieParams(jobId: String, oozieUrl: String): Map[String, String] =
    getOoziePropertyReader(jobId, "all", oozieUrl)._allProperties

  def getOozieParam(jobId: String, propertyName: String, oozieUrl: String): String =
    getOoziePropertyReader(jobId, propertyName, oozieUrl).propertyValue

  private def getOoziePropertyReader(jobId: String, propertyName: String, oozieUrl: String): OozieParamsReader = {
    if (_reader.isEmpty)
      _reader = Some(new OozieParamsReader(jobId, propertyName, oozieUrl))

    _reader.get
  }
}

import org.apache.oozie.client.{AuthOozieClient, WorkflowJob}

import java.net.URLDecoder

class OozieParamsReader(jobId: String, get: String, OOZIE_URL: String) {

  private val client = new AuthOozieClient(OOZIE_URL)

  private val job: WorkflowJob = client.getJobInfo(jobId)

  private val xmlJobConfig = scala.xml.XML.loadString(job.getConf)

  private val xmlProperties: scala.xml.NodeSeq = xmlJobConfig \ "property"

  private val xmlProperty: scala.xml.NodeSeq = xmlProperties.filter(node => (node \ "name").text == get)

  val propertyValue: String = URLDecoder.decode((xmlProperty \ "value").text, "UTF-8")

  val _allProperties: Map[String, String] =
    xmlProperties.map { node =>
      ((node \ "name").text, (node \ "value").text)
    }.toMap

  def allProperties = {
    _allProperties.map(a => {
      if (a._1 == "oozie" || a._1 == "ctl") {
        if (a._2.contains('\n'))
          s"${a._1}.url=" + tripleQuotesStr() + a._2 + tripleQuotesStr()
        else
          s"${a._1}.url=${'"'}${a._2}${'"'}"
      } else {
        if (a._2.contains('\n'))
          s"${a._1}=" + tripleQuotesStr() + a._2 + tripleQuotesStr()
        else
          s"${a._1}=${'"'}${a._2}${'"'}"
      }
    }).mkString("", "\n", "")
  }

  def tripleQuotesStr(): String = {
    """"""" + """"""" + """""""
  }
}
