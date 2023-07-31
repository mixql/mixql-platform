package org.mixql.remote.messages.module.worker;

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
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        for (int i = 0; i <= names.length - 1; i++) {
            buffer.append(names[i]);
            if (i != names.length - 1) {
                buffer.append(", ");
            }
        }
        buffer.append("]");
        return "{ type: " + type() + "sender: " + sender() + "params: " + buffer +
                " clientAddress: " + new String(clientAddress()) + " }";
    }
}
