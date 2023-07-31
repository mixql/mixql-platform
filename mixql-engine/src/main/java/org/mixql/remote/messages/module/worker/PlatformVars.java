package org.mixql.remote.messages.module.worker;

import org.mixql.remote.RemoteMessageConverter;
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
