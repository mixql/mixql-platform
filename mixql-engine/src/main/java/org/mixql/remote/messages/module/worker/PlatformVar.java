package org.mixql.remote.messages.module.worker;

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
        return "{ " + "sender: " + sender() + " type: " + type() + " mame: " + name +
                " msg: " + msg + " clientAddress: " + new String(clientAddress()) + " }";
    }
}
