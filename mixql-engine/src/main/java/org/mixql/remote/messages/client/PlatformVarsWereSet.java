package org.mixql.remote.messages.client;

import org.mixql.remote.RemoteMessageConverter;

import java.util.ArrayList;

public class PlatformVarsWereSet implements IWorkerReceiver {
    public ArrayList<String> names;

    @Override
    public String workerIdentity() {
        return _sender;
    }

    private String _sender;
    public String moduleIdentity;
    private String clientIdentity;


    public PlatformVarsWereSet(String moduleIdentity, String sender, ArrayList<String> names) {
        _sender = sender;
        this.names = names;
        this.moduleIdentity = moduleIdentity;
    }

    public PlatformVarsWereSet(String moduleIdentity, String clientIdentity, String sender, ArrayList<String> names) {
        this(moduleIdentity, sender, names);
        this.clientIdentity = clientIdentity;
    }

    @Override
    public IModuleReceiver SetClientIdentity(String clientIdentity) {
        this.clientIdentity = clientIdentity;
        return this;
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

    @Override
    public String clientIdentity() {
        return this.clientIdentity;
    }
}
