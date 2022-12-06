package org.mixql.engine.demo

trait IModuleExecutor {

  def reactOnMessage(clientAddress: Array[Byte], msg: Array[Byte])
                    (implicit identity: String, clientAddressStr: String): Unit
}
