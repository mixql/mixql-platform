package org.mixql.engine.test

import org.mixql.core.context.EngineContext
import org.mixql.core.context.gtype.Type
import org.mixql.engine.core.PlatformContext
import org.mixql.engine.core.logger.ModuleLogger

import scala.collection.mutable

class PlatformContextTest(engineParams: mutable.Map[String, Type], identity: String)
  extends PlatformContext(null, "", null)(new ModuleLogger(identity)) {

  val ctx = new EngineContext(
    new org.mixql.core.context.Context(
      mutable.Map("stub" -> new org.mixql.core.test.engines.StubEngine()), "stub",
      mutable.Map(), engineParams
    )
  )

  override def setVar(key: String, value: Type): Unit = {
    ctx.setVar(key, value)
  }

  override def getVar(key: String): Type = {
    ctx.getVar(key)
  }

  override def getVars(keys: List[String]): mutable.Map[String, Type] = {
    ctx.getVars(keys)
  }

  override def setVars(vars: Map[String, Type]): Unit = {
    ctx.setVars(vars)
  }

  override def getVarsNames(): List[String] = {
    ctx.getVarsNames()
  }
}
