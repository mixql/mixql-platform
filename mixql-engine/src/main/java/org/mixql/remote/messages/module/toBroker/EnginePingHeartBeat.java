package org.mixql.remote.messages.module.toBroker;

import org.mixql.remote.RemoteMessageConverter;

public class EnginePingHeartBeat implements IBrokerReceiverFromModule {
    private String engineName;

    @Override
    public String engineName() {
        return engineName;
    }

    public EnginePingHeartBeat(String engineName) {
        this.engineName = engineName;
    }

    public EnginePingHeartBeat() {
    }

    @Override
    public IBrokerReceiverFromModule setEngineName(String engineName) {
        this.engineName = engineName;
        return this;
    }

    @Override
    public String toString() {
        try {
            return RemoteMessageConverter.toJson(this);
        } catch (Exception e) {
            System.out.println(
                    String.format("Error while toString of class type %s, exception: %s\nUsing default toString",
                            type(), e.getMessage())
            );
            return super.toString();
        }
    }
}
