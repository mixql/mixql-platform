package org.mixql.remote.messages.rtype;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;

public class Error implements Message {
    private String errorMsg;

    public Error(String msg) {
        this.errorMsg = msg;
    }

    public String getErrorMessage() {
        return errorMsg;
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
