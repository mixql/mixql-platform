package org.mixql.remote.messages.module.worker;

import org.mixql.remote.RemoteMessageConverter;

public class GetPlatformVar implements IWorkerSendToPlatform {
    public String name;

    public String _sender;
    private byte[] _clientAddress;

    @Override
    public byte[] clientAddress() {
        return _clientAddress;
    }

    @Override
    public String sender() {
        return _sender;
    }

    public GetPlatformVar(String sender, String key, byte[] clientAddress) {
        this._sender = sender;
        this.name = key;
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
