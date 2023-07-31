package org.mixql.remote.messages.module.worker;

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
        return "{ " + "sender: " + sender() + " type: " + type() + " mame: " + name +
                " clientAddress: " + new String(clientAddress()) + " }";
    }
}
