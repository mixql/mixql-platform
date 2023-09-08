package org.mixql.remote.messages.module.toBroker;

import org.mixql.remote.messages.Message;

public interface IBrokerReceiver extends Message {
    String engineName();
}
