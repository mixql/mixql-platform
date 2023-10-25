package org.mixql.test.utils

import java.io.IOException
import java.io.File

object JavaProcess {

  @throws(classOf[IOException])
  @throws(classOf[InterruptedException])
  def exec(klass: Class[_], args: List[String] = List()): Process = {
    val javaHome: String = System.getProperty("java.home");
    val javaBin: String =
      javaHome +
        File.separator + "bin" +
        File.separator + "java";
    val classpath: String = System.getProperty("java.class.path");
    val className: String = klass.getName();

    val command: List[String] = List(javaBin, "-cp", classpath, className) ++ args

    val builder: ProcessBuilder = new ProcessBuilder(command: _*);

//    val process: Process = builder.inheritIO().start();
//    process.waitFor();
//    return process.exitValue();
    builder.inheritIO().start();
  }

}
