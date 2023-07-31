package org.mixql.remote.messages.module.worker;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;
import org.mixql.remote.messages.gtype.gString;

import java.util.ArrayList;

public class GetPlatformVars implements IWorkerSendToPlatform {

    public String[] names;

    private String _sender;
    private byte[] _clientAddress;


    @Override
    public byte[] clientAddress() {
        return _clientAddress;
    }

    @Override
    public String sender() {
        return _sender;
    }

    public GetPlatformVars(String sender, String[] keys, byte[] clientAddress) {
        _sender = sender;
        names = keys;
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
