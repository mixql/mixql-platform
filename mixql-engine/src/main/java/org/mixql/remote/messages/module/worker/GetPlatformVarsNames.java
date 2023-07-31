package org.mixql.remote.messages.module.worker;

import org.mixql.remote.RemoteMessageConverter;

public class GetPlatformVarsNames implements IWorkerSendToPlatform {

    @Override
    public String sender() {
        return _sender;
    }

    private String _sender;

    private byte[] _clientAddress;

    @Override
    public byte[] clientAddress() {
        return _clientAddress;
    }

    public GetPlatformVarsNames(String sender, byte[] clientAddress) {
        _sender = sender;
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
