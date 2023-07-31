package org.mixql.engine.core;

import org.mixql.engine.core.logger.ModuleLogger;
import org.mixql.remote.messages.*;
import org.mixql.remote.messages.module.DefinedFunctions;
import org.mixql.remote.messages.module.Execute;
import org.mixql.remote.messages.module.ExecuteFunction;
import org.mixql.remote.messages.module.ParamChanged;

public interface IModuleExecutor {

    Message reactOnExecute(Execute msg, String identity, String clientAddress, ModuleLogger logger,
                           PlatformContext platformContext);

    void reactOnParamChanged(ParamChanged msg, String identity, String clientAddress, ModuleLogger logger,
                             PlatformContext platformContext);

    void reactOnShutDown(String identity, String clientAddress, ModuleLogger logger);

    Message reactOnExecuteFunction(ExecuteFunction msg, String identity, String clientAddress, ModuleLogger logger,
                                   PlatformContext platformContext);

    DefinedFunctions reactOnGetDefinedFunctions(String identity, String clientAddress, ModuleLogger logger);
}
