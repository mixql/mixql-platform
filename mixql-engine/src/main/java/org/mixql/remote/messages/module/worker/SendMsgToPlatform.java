package org.mixql.remote.messages.module.worker;

import org.mixql.remote.messages.Message;

public class SendMsgToPlatform implements IWorkerSendToPlatform {
    private byte[] _clientAddress;
    public Message msg;

    @Override
    public String sender() {
        return _sender;
    }
    private String _sender;

    @Override
    public byte[] clientAddress() {
        return _clientAddress;
    }

    public SendMsgToPlatform(byte[] clientAddress, Message msg, String workerID){
        _clientAddress = clientAddress;
        this.msg = msg;
        _sender = workerID;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + " clientAddress: " + new String(clientAddress()) + " msg: " + msg +
                " clientAddress: " + new String(clientAddress()) + " }";
    }
    
}
