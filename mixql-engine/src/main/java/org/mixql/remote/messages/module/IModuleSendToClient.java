package org.mixql.remote.messages.module;

import org.mixql.remote.messages.Message;

public interface IModuleSendToClient extends Message {
    public String clientIdentity();
}
