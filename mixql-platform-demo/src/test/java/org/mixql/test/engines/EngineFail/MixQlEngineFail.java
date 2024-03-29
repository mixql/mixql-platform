package org.mixql.test.engines.EngineFail;

import org.mixql.engine.core.logger.ModuleLogger;
import org.mixql.engine.core.Module;

public class MixQlEngineFail {
    public static void main(String args[]) {

        String indentity = args[5];
        String host = args[3];
        String port = args[1];
        String logLevel = args[7];
        ModuleLogger logger = new ModuleLogger(indentity, logLevel);
        logger.logInfo("Starting MixQlEngineFail");

        new Module(new EngineFailExecutor(), indentity, host, Integer.parseInt(port), logger).startServer();
    }
}
