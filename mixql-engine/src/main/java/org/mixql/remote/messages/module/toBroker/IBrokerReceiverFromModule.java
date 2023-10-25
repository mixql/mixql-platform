package org.mixql.remote.messages.module.toBroker;

import org.mixql.remote.messages.Message;

public interface IBrokerReceiverFromModule extends Message {
    String engineName();
    IBrokerReceiverFromModule setEngineName(String identity);
}
