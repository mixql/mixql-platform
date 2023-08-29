package org.mixql.remote.messages.cluster;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;

public class EngineStarted implements Message {
    public String engineName;

    public EngineStarted(String engineName) {
        this.engineName = engineName;
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
}
