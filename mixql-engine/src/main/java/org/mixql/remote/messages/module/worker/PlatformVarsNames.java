package org.mixql.remote.messages.module.worker;

public class PlatformVarsNames implements IWorkerSendToPlatform {
    public String[] names;

    @Override
    public String sender() {
        return _sender;
    }
    private String _sender;
    private byte[] _clientAddress;

    public PlatformVarsNames(String sender, String[] names, byte[] clientAddress) {
        _sender = sender;
        this.names = names;
        _clientAddress = clientAddress;
    }

    @Override
    public byte[] clientAddress() {
        return _clientAddress;
    }

    @Override
    public String toString() {
        return "{ " + "sender: " + sender() + " type: " + type() +
                " names: " + names.toString() + " clientAddress: " + new String(clientAddress()) + " }";
    }
}
