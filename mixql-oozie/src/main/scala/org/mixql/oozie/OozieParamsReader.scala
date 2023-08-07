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
import org.w3c.dom.{Element, Node, NodeList}

import java.net.URLDecoder
import javax.xml.parsers.{DocumentBuilder, DocumentBuilderFactory}

class OozieParamsReader(jobId: String, get: String, OOZIE_URL: String) {

  private val client = new AuthOozieClient(OOZIE_URL)

  private val job: WorkflowJob = client.getJobInfo(jobId)

  private val builder: DocumentBuilder = DocumentBuilderFactory.newInstance.newDocumentBuilder
  private val xmlJobConfig: org.w3c.dom.Document = builder.parse(job.getConf)

  private val xmlProperties: NodeList = xmlJobConfig.getElementsByTagName("property")

  val propertyValue: String = URLDecoder.decode(_allProperties(get), "UTF-8")

  val _allProperties: Map[String, String] = {
    val size = xmlProperties.getLength
    val propertiesList =
      for (i <- 0 until size if xmlProperties.item(i).getNodeType() == Node.ELEMENT_NODE)
        yield xmlProperties.item(i).asInstanceOf[Element]

    var propertiesMap: Map[String, String] = Map()

    propertiesList.foreach(property =>
      propertiesMap = propertiesMap.updated(
        property.getElementsByTagName("name").item(0).getTextContent,
        property.getElementsByTagName("value").item(0).getTextContent
      )
    )
    propertiesMap
  }

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
