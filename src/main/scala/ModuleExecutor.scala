package org.mixql.engine.core

import org.zeromq.ZMQ

trait IModuleExecutor {

  def reactOnMessage(msg: Array[Byte])
                    (implicit server: ZMQ.Socket, identity: String, clientAddress: Array[Byte]): Unit
}
