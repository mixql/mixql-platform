package org.mixql.remote.messages.module.toBroker;

import org.mixql.remote.RemoteMessageConverter;

public class EnginePingHeartBeat implements IBrokerReceiver {
    private String engineName;

    @Override
    public String engineName() {
        return engineName;
    }


    public EnginePingHeartBeat(String engineName) {
        this.engineName = engineName;
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
