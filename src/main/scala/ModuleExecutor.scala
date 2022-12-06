package org.mixql.engine.core

trait IModuleExecutor {

  def reactOnMessage(clientAddress: Array[Byte], msg: Array[Byte])
                    (implicit identity: String, clientAddressStr: String): Unit
}
