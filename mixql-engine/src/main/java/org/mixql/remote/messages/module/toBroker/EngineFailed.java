package org.mixql.remote.messages.module.toBroker;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.rtype.Error;

public class EngineFailed extends Error implements IBrokerReceiverFromModule {
    private String engineName;

    @Override
    public String engineName() {
        return engineName;
    }


    public EngineFailed(String engineName, String errorMsg) {
        super(errorMsg);
        this.engineName = engineName;
    }

    public EngineFailed(String errorMsg) {
        super(errorMsg);
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
