package org.mixql.remote.messages.module.worker;

import org.mixql.remote.messages.Message;

public interface IWorkerSendToPlatform extends IWorkerSender {
    public byte[] clientAddress();
}
