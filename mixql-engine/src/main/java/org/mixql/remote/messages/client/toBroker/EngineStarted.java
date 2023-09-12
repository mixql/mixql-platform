package org.mixql.remote.messages.client.toBroker;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;

public class EngineStarted implements IBrokerReceiverFromClient {
    public String engineName;
    private String clientIdentity;


    public EngineStarted(String engineName, String clientIdentity) {
        this.engineName = engineName;
        this.clientIdentity = clientIdentity;
    }

    @Override
    public String toString() {
        try {
            return RemoteMessageConverter.toJson(this);
        } catch (Exception e) {
            System.out.printf("Error while toString of class type %s, exception: %s\nUsing default toString%n",
                            type(), e.getMessage());
            return super.toString();
        }
    }

    @Override
    public String clientIdentity() {
        return this.clientIdentity;
    }
}
