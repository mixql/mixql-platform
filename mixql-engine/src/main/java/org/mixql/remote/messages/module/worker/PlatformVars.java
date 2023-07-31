package org.mixql.remote.messages.module.worker;

import org.mixql.remote.messages.module.Param;

public class PlatformVars implements IWorkerSendToPlatform {
    public Param[] vars;

    @Override
    public String sender() {
        return _sender;
    }
    private String _sender;

    @Override
    public byte[] clientAddress() {
        return _clientAddress;
    }
    private byte[] _clientAddress;


    public PlatformVars(String sender, Param[] params, byte[] clientAddress) {
        _sender = sender;
        this.vars = params;
        _clientAddress = clientAddress;
    }

    @Override
    public String toString() {
        return "{ " + "sender: " + sender() + " type: " + type() +
                " vars: " + vars + " clientAddress: " + new String(clientAddress()) + " }";
    }
}
