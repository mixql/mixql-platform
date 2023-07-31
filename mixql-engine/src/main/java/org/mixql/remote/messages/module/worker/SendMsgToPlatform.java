package org.mixql.remote.messages.module.worker;

import org.mixql.remote.RemoteMessageConverter;
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
        try {
            return RemoteMessageConverter.toJson(this);
        } catch (Exception e) {
            System.out.println(
                    String.format("Error while toString of class type %s, exception: %s\nUsing default toString",
                            type(), e.getMessage())
            );
            return super.toString();
        }
    }
    
}
