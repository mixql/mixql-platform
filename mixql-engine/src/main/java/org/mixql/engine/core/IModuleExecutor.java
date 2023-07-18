package org.mixql.engine.core;

import org.mixql.protobuf.messages.*;
import org.mixql.engine.core.logger.ModuleLogger;

public interface IModuleExecutor {

    Message reactOnExecute(Execute msg, String identity, String clientAddress, ModuleLogger logger);

    ParamWasSet reactOnSetParam(SetParam msg, String identity, String clientAddress, ModuleLogger logger);

    Message reactOnGetParam(GetParam msg, String identity, String clientAddress, ModuleLogger logger);

    Bool reactOnIsParam(IsParam msg, String identity, String clientAddress, ModuleLogger logger);

    void reactOnShutDown(String identity, String clientAddress, ModuleLogger logger);

    Message reactOnExecuteFunction(ExecuteFunction msg, String identity, String clientAddress, ModuleLogger logger);

    DefinedFunctions reactOnGetDefinedFunctions(String identity, String clientAddress, ModuleLogger logger);
}
