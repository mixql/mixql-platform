package org.mixql.remote.messages.module.worker;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;
import org.mixql.remote.messages.module.IModuleSendToClient;

public class SetPlatformVar implements IWorkerSendToClient {
    public String name;
    public Message msg;

    private String _clientAddress;

    @Override
    public String clientIdentity() {
        return _clientAddress;
    }

    @Override
    public String workerIdentity() {
        return _sender;
    }

    private String _sender;

    public SetPlatformVar(String sender, String name, Message msg, String clientAddress) {
        _sender = sender;
        this.name = name;
        this.msg = msg;
        _clientAddress = clientAddress;
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
