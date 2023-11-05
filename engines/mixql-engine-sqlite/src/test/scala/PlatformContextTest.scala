import org.apache.logging.log4j.LogManager
import org.mixql.core.context.EngineContext
import org.mixql.core.context.mtype.MType
import org.mixql.engine.core.PlatformContext
import org.mixql.engine.core.logger.ModuleLogger

import scala.collection.mutable

class PlatformContextTest(engineParams: mutable.Map[String, MType], identity: String)
    extends PlatformContext(null, "", null)(new ModuleLogger(identity, LogManager.getRootLogger.getLevel.name())) {

  val ctx =
    new EngineContext(
      org.mixql.core.context.Context(
        mutable.Map("stub" -> new org.mixql.core.test.engines.StubEngine()),
        "stub",
        mutable.Map(),
        engineParams
      ),
      "stub"
    )

  override def setVar(key: String, value: MType): Unit = {
    ctx.setVar(key, value)
  }

  override def getVar(key: String): MType = {
    ctx.getVar(key)
  }

  override def getVars(keys: List[String]): mutable.Map[String, MType] = {
    ctx.getVars(keys)
  }

  override def setVars(vars: Map[String, MType]): Unit = {
    ctx.setVars(vars)
  }

  override def getVarsNames(): List[String] = {
    ctx.getVarsNames()
  }
}
