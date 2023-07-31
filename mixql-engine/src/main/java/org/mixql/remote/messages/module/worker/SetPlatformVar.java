package org.mixql.remote.messages.module.worker;

import org.mixql.remote.messages.Message;

public class SetPlatformVar implements IWorkerSendToPlatform {
    public String name;
    public Message msg;

    private byte[] _clientAddress;

    @Override
    public byte[] clientAddress() {
        return _clientAddress;
    }

    @Override
    public String sender() {
        return _sender;
    }

    private String _sender;

    public SetPlatformVar(String sender, String name, Message msg, byte[] clientAddress) {
        _sender = sender;
        this.name = name;
        this.msg = msg;
        _clientAddress = clientAddress;
    }

    @Override
    public String toString() {
        return "{ " + "sender: " + sender() + " type: " + type() + " mame: " + name + " msg: " + msg +
                " clientAddress: " + new String(clientAddress()) + " }";
    }
}
