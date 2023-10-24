package org.mixql.remote.messages.module.toBroker;

import org.mixql.remote.RemoteMessageConverter;

public class EngineIsReady implements IBrokerReceiverFromModule {
    private String engineName;
    private Long heartBeatInterval;
    private Long pollerTimeout;

    @Override
    public String engineName() {
        return engineName;
    }

    public EngineIsReady(String engineName, Long heartBeatInterval, Long pollerTimeout) {
        this(heartBeatInterval, pollerTimeout);
        this.engineName = engineName;
    }

    public Long getHeartBeatInterval() {
        return heartBeatInterval;
    }

    public Long getPollerTimeout() {
        return pollerTimeout;
    }

    public EngineIsReady(Long heartBeatInterval, Long pollerTimeout) {
        this.heartBeatInterval = heartBeatInterval;
        this.pollerTimeout = pollerTimeout;
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
