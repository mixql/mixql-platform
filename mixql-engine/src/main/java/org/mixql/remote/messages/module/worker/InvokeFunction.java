package org.mixql.remote.messages.module.worker;

import org.mixql.core.context.gtype.Type;
import org.mixql.remote.GtypeConverter;
import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;

public class InvokeFunction implements IWorkerSendToPlatform {

    public String name;

    private String _sender;
    private byte[] _clientAddress;

    public Message[] args;


    @Override
    public byte[] clientAddress() {
        return _clientAddress;
    }

    @Override
    public String sender() {
        return _sender;
    }

    public InvokeFunction(String sender, String funcName, Message[] args, byte[] clientAddress) {
        _sender = sender;
        name = funcName;
        _clientAddress = clientAddress;
        this.args = args;
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
