package org.mixql.remote.messages.module;

import org.mixql.remote.RemoteMessageConverter;

public class DefinedFunctions implements IModuleSendToClient {
    public String[] arr;

    private byte[] _clientAddress;

    public DefinedFunctions(String[] arr, byte[] clientAddress) {
        this.arr = arr;
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

    @Override
    public byte[] clientIdentity() {
        return _clientAddress;
    }
}
