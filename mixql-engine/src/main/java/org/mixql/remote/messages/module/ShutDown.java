package org.mixql.remote.messages.module;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;

public class ShutDown implements Message {
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
