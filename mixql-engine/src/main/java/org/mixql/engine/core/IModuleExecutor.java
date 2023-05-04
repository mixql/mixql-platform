package org.mixql.engine.core;

import org.mixql.protobuf.messages.*;

public interface IModuleExecutor {

    Message reactOnExecute(Execute msg, String identity, String clientAddress);

    ParamWasSet reactOnSetParam(SetParam msg, String identity, String clientAddress);

    Message reactOnGetParam(GetParam msg, String identity, String clientAddress);

    Bool reactOnIsParam(IsParam msg, String identity, String clientAddress);

    void reactOnShutDown(String identity, String clientAddress);

    Message reactOnExecuteFunction(ExecuteFunction msg, String identity, String clientAddress);

    DefinedFunctions reactOnGetDefinedFunctions(String identity, String clientAddress);
}
