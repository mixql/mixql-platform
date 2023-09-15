package org.mixql.remote.messages.client;

import org.mixql.remote.RemoteMessageConverter;

public class PlatformVarsNames implements IWorkerReceiver {
    public String[] names;

    @Override
    public String workerIdentity() {
        return _sender;
    }
    private String _sender;
    public String moduleIdentity;

    private String clientIdentity;

    @Override
    public String clientIdentity() {
        return this.clientIdentity;
    }


    public PlatformVarsNames(String moduleIdentity, String clientIdentity, String sender, String[] names) {
        _sender = sender;
        this.names = names;
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
