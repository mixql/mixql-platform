package org.mixql.remote.messages.module.worker;

import org.mixql.remote.messages.Message;

import java.util.Map;

public class SetPlatformVars implements IWorkerSendToPlatform {
    public Map<String, Message> vars;
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

    public SetPlatformVars(String sender, Map<String, Message> vars, byte[] clientAddress){
        _sender = sender;
        this.vars = vars;
        _clientAddress = clientAddress;
    }

    @Override
    public String toString() {
        return "{ " + "sender: " + sender() + " type: " + type()  + " vars: " + vars.toString() +
                " clientAddress: " + new String(clientAddress()) + " }";
    }
}
