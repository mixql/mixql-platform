package org.mixql.remote.messages.module.worker;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;
import org.mixql.remote.messages.module.IModuleSendToClient;

public class InvokeFunction implements IWorkerSendToClient {

    public String name;

    private String _sender;
    private String _clientAddress;

    public Message[] args;


    @Override
    public String clientIdentity() {
        return _clientAddress;
    }

    @Override
    public String workerIdentity() {
        return _sender;
    }

    public InvokeFunction(String sender, String funcName, Message[] args, String clientAddress) {
        _sender = sender;
        name = funcName;
        _clientAddress = clientAddress;
        this.args = args;
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
