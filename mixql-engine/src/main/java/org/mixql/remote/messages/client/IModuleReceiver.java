package org.mixql.remote.messages.client;

import org.mixql.remote.messages.Message;

public interface IModuleReceiver extends Message {
    public String moduleIdentity();

    public String clientIdentity();
}
