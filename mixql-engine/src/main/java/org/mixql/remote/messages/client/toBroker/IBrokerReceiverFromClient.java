package org.mixql.remote.messages.client.toBroker;

import org.mixql.remote.messages.Message;

public interface IBrokerReceiverFromClient extends Message {
    public String clientIdentity();

    public IBrokerReceiverFromClient SetClientIdentity(String clientIdentity);
}
