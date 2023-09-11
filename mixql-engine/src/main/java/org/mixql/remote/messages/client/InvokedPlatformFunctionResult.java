package org.mixql.remote.messages.client;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;

public class InvokedPlatformFunctionResult implements IWorkerReceiver {
    public String name;

    public String _sender;

    public Message result;

    public String moduleIdentity;

    private String clientIdentity;

    @Override
    public String clientIdentity() {
        return this.clientIdentity;
    }

    @Override
    public String workerIdentity() {
        return _sender;
    }

    public InvokedPlatformFunctionResult(String moduleIdentity, String clientIdentity, String sender,
                                         String funcName, Message result) {
        this._sender = sender;
        this.name = funcName;
        this.result = result;
        this.moduleIdentity = moduleIdentity;
        this.clientIdentity = clientIdentity;
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
    public String moduleIdentity() {
        return moduleIdentity;
    }
}
