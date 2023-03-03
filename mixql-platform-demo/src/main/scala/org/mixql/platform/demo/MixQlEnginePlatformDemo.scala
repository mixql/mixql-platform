package org.mixql.platform.demo

import MixQlEnginePlatformDemo.code
import org.mixql.core.run
import org.rogach.scallop.ScallopConf

import java.io.File
import org.mixql.cluster.{BrokerModule, ClientModule}
import org.mixql.core.engine.Engine
import org.mixql.net.PortOperations
import org.mixql.core.context.Context
import org.mixql.protobuf.messages.clientMsgs.ShutDown

import scala.collection.mutable

object MixQlEnginePlatformDemo:
  def main(args: Array[String]): Unit =
    println("Mixql engine demo platform: parsing args")
    val (host, portFrontend, portBackend, basePath, sqlScriptFiles) = parseArgs(args.toList)

    println(s"Mixql engine demo platform: Starting broker messager with" +
      s" frontend port $portFrontend and backend port $portBackend on host $host")
    val broker = new BrokerModule(portFrontend, portBackend, host)
    broker.start()

    println(s"Mixql engine demo platform: initialising engines")
    val engines = mutable.Map[String, Engine]("demo" -> new ClientModule(
      //Name of client, is used for identification in broker,
      //must be unique
      "mixql-engine-stub-demo-platform",
      //Name of remote engine, is used for identification in broker,
      //must be unique
      "mixql-engine-stub",
      //will be started mixql-engine-demo on linux or mixql-engine-demo.bat on windows
      //in base path
      Some("mixql-engine-stub"),
      None,
      host, portFrontend, portBackend, basePath
    ),
      "sqlite" -> new ClientModule(
        //Name of client, is used for identification in broker,
        //must be unique
        "mixql-engine-sqlite-demo-platform",
        //Name of remote engine, is used for identification in broker,
        //must be unique
        "mixql-engine-sqlite",
        //will be started mixql-engine-demo on linux or mixql-engine-demo.bat on windows
        //in base path
        Some("mixql-engine-sqlite"),
        None,
        host, portFrontend, portBackend, basePath
      )
    )

    println(s"Mixql engine demo platform: init Cluster context")
    val context =
      new Context(engines, "demo")

    try {
      println(s"Mixql engine demo platform: reading and executing sql files if they exist")
      (sqlScriptFiles match {
        case None => List((None, code))
        case Some(value) => value.map {
          (f: File) => (Some(f.getAbsolutePath), utils.FilesOperations.readFileContent(f))
        }
      }).foreach(sql => {
        if sql._1.nonEmpty then
          println("Mixql engine demo platform: running script: " + sql._1.get)
        else
          println("Mixql engine demo platform: running standard code for testing: " + code)
        run(sql._2, context)
      })

      println(context.scope.head)
    } catch {
      case e: Throwable => println(s"Error: Mixql engine demo platform: " + e.getMessage)
    } finally {
      context.engines.values.foreach(
        e => if (e.isInstanceOf[ClientModule]) {
          val cl: ClientModule = e.asInstanceOf[ClientModule]
          println(s"mixql core context: sending shutdwon to remote engine " + cl.name)
          cl.sendMsg(ShutDown())
        }
      )
      context.close()
      broker.close()
    }

  def parseArgs(args: List[String]): (String, Int, Int, File, Option[List[File]]) =
    import org.rogach.scallop.ScallopConfBase
    val appArgs = AppArgs(args)
    val host: String = appArgs.host.toOption.get
    val portFrontend = PortOperations.isPortAvailable(
      appArgs.portFrontend.toOption.get
    )
    val portBackend = PortOperations.isPortAvailable(
      appArgs.portBackend.toOption.get
    )
    val basePath = appArgs.basePath.toOption.get
    val sqlScripts = appArgs.sqlFile.toOption
    (host, portFrontend, portBackend, basePath, sqlScripts)

  val code =
    """some code;
      |let gg = 12.4 - 11.2;
      |let wp.x = $gg > 11;
      |let res = 'one' + 'two';
      |let check_case = case when 1 > 2 then 12 when 1 < 2 then 13 else '12g' end;
      |if 12 < 11 then
      |  print(true);
      |else
      |  print(false);
      |end if
      |let x = 0;
      |while $x < 5 do
      |  print($x);
      |  let x = $x + 1;
      |end while
      |for i in 1..20 step 2 loop
      |  print($i);
      |end loop
      |select '${$gg + 2}' from wp;
      |select '${$gg * 5}' from wp;
      |print("\$res");
      |print("$res");
      |print("${$res}");
      |print("${'${'$res'+'a'}'}\"");
      |let t="${'1'+$res}";
      |let t="123;
      |   \n 456";
      |print($t);
      |let t="${'1'+ '${$res}'}";
      |print($t);
      |some end;
      |print(current_timestamp);
      |let arr = [3, 'gg'];
      |let arr[0] = 4;
      |print($arr[0]);
      |/*
      |let mapa = {1: 1, "1": 2};
      |print($mapa[1]);
      |print($mapa["1"]);
      |*/
      |""".stripMargin

case class AppArgs(arguments: Seq[String]) extends ScallopConf(arguments):

  import org.rogach.scallop.stringConverter
  import org.rogach.scallop.intConverter
  import org.rogach.scallop.fileConverter
  import org.rogach.scallop.fileListConverter

  val portFrontend = opt[Int](required = false, default = Some(0))
  val portBackend = opt[Int](required = false, default = Some(0))
  val host = opt[String](required = false, default = Some("0.0.0.0"))
  val basePath = opt[File](required = false, default = Some(new File(".")))
  val sqlFile = opt[List[File]](descr = "path to sql script file", required = false)

  validateFilesIsFile(sqlFile)
  validateFilesExist(sqlFile)

  validateFileIsDirectory(basePath)

  verify()


