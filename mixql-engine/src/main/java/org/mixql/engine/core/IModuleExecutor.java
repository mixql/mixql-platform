package org.mixql.engine.core;

import org.mixql.engine.core.logger.ModuleLogger;
import org.mixql.remote.messages.*;
import org.mixql.remote.messages.module.DefinedFunctions;
import org.mixql.remote.messages.client.Execute;
import org.mixql.remote.messages.client.ExecuteFunction;

public interface IModuleExecutor {

    Message reactOnExecuteAsync(Execute msg, String identity, String clientAddress, ModuleLogger logger,
                                PlatformContext platformContext);

    void reactOnShutDown(String identity, String clientAddress, ModuleLogger logger);

    Message reactOnExecuteFunctionAsync(ExecuteFunction msg, String identity, String clientAddress, ModuleLogger logger,
                                        PlatformContext platformContext);

    DefinedFunctions reactOnGetDefinedFunctions(String identity, String clientAddress, ModuleLogger logger);
}
