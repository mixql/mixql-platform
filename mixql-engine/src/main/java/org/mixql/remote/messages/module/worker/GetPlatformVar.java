package org.mixql.remote.messages.module.worker;

public class GetPlatformVar implements IWorkerSendToPlatform {
    public String name;

    public String _sender;
    private byte[] _clientAddress;

    @Override
    public byte[] clientAddress() {
        return _clientAddress;
    }

    @Override
    public String sender() {
        return _sender;
    }

    public GetPlatformVar(String sender, String key, byte[] clientAddress){
        this._sender = sender;
        this.name = key;
        _clientAddress = clientAddress;
    }

    @Override
    public String toString() {
        return "{ " + "sender: " + sender() + " type: " + type() + " mame: " + name +
                " clientAddress: " + new String(clientAddress()) + " }";
    }
}
