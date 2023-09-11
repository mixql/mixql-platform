package org.mixql.remote.messages.client;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.type.Param;

public class PlatformVars implements IWorkerReceiver {
    public Param[] vars;
    public String moduleIdentity;

    @Override
    public String workerIdentity() {
        return _sender;
    }

    private String _sender;

    private String clientIdentity;

    @Override
    public String clientIdentity() {
        return this.clientIdentity;
    }

    public PlatformVars(String moduleIdentity, String clientIdentity, String sender, Param[] params) {
        _sender = sender;
        this.vars = params;
        this.moduleIdentity = moduleIdentity;
        this.clientIdentity = clientIdentity;
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

    @Override
    public String moduleIdentity() {
        return moduleIdentity;
    }

}
