package org.mixql.remote.messages.client;

import org.mixql.remote.messages.Message;

public interface IWorkerSender extends Message {
    public String sender();
}