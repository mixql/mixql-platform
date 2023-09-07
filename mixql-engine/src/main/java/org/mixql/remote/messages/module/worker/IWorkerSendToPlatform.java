package org.mixql.remote.messages.module.worker;

import org.mixql.remote.messages.client.IWorkerSender;

public interface IWorkerSendToPlatform extends IWorkerSender {
    public byte[] clientAddress();
}
