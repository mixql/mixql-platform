package org.mixql.remote.messages.module.worker;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;

public class PlatformVar implements IWorkerSendToPlatform {
    public String name;
    public Message msg;

    private String _sender;

    @Override
    public String sender() {
        return _sender;
    }

    private byte[] _clientAddress;

    @Override
    public byte[] clientAddress() {
        return _clientAddress;
    }

    public PlatformVar(String sender, String key, Message msg, byte[] clientAddress) {
        _sender = sender;
        this.name = key;
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
