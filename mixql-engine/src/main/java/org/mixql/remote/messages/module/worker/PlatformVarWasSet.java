package org.mixql.remote.messages.module.worker;

import org.mixql.remote.RemoteMessageConverter;

public class PlatformVarWasSet implements IWorkerSendToPlatform {
    public String name;

    @Override
    public String sender() {
        return _sender;
    }
    private String _sender;

    private byte[] _clientAddress;

    public PlatformVarWasSet(String sender, String name, byte[] clientAddress) {
        _sender = sender;
        this.name = name;
        _clientAddress = clientAddress;
    }

    @Override
    public byte[] clientAddress() {
        return _clientAddress;
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
