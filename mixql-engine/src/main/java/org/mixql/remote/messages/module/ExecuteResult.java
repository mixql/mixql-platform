package org.mixql.remote.messages.module;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;

public class ExecuteResult implements IModuleSendToClient {
    public String stmt;

    public Message result;

    private String clientAddress;

    public ExecuteResult(String stmt, Message result, String clientAddress) {
        this.stmt = stmt;
        this.result = result;
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
