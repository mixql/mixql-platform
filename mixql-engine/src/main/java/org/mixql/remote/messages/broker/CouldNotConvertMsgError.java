package org.mixql.remote.messages.broker;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.rtype.Error;

public class CouldNotConvertMsgError extends Error implements IBrokerSender {

    public CouldNotConvertMsgError(String errorMsg) {
        super(errorMsg);
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
