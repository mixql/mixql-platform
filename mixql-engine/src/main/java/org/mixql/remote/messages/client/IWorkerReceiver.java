package org.mixql.remote.messages.client;

import org.mixql.remote.messages.Message;

public interface IWorkerReceiver extends IModuleReceiver {
    public String workerIdentity();
}
