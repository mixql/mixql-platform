package org.mixql.remote.messages.module;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;

public class ExecutedFunctionResult implements IModuleSendToClient {
    public String functionName;
    public Message msg;

    private String clientAddress;

    public ExecutedFunctionResult(String functionName, Message msg, String clientAddress) {
        this.functionName = functionName;
        this.msg = msg;
        this.clientAddress = clientAddress;
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

    @Override
    public String clientIdentity() {
        return clientAddress;
    }

}
