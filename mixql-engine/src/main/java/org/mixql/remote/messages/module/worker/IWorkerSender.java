package org.mixql.remote.messages.module.worker;

import org.mixql.remote.messages.Message;

public interface IWorkerSender extends Message {
    public String workerIdentity();
}
