package org.mixql.remote.messages.module.worker;

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
        return "{ type: " + type() + "sender: " + sender() +
                " clientAddress: " + new String(clientAddress()) + " }";
    }
}
