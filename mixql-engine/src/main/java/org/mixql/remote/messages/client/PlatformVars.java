package org.mixql.remote.messages.client;

import org.mixql.remote.RemoteMessageConverter;

public class PlatformVars implements IWorkerSender {
    public Param[] vars;

    @Override
    public String sender() {
        return _sender;
    }
    private String _sender;

    public PlatformVars(String sender, Param[] params) {
        _sender = sender;
        this.vars = params;
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
