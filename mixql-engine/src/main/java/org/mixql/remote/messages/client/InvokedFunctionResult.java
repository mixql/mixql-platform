package org.mixql.remote.messages.client;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;

public class InvokedFunctionResult implements IWorkerSender {
    public String name;

    public String _sender;

    public Message result;

    @Override
    public String sender() {
        return _sender;
    }

    public InvokedFunctionResult(String sender, String funcName, Message result) {
        this._sender = sender;
        this.name = funcName;
        this.result = result;
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
