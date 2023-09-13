package org.mixql.remote.messages.broker;

import org.mixql.remote.RemoteMessageConverter;

public class PlatformPongHeartBeat implements IBrokerSender {
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
