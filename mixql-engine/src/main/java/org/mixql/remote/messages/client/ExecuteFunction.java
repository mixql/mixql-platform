package org.mixql.remote.messages.client;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;

public class ExecuteFunction implements IModuleReceiver {

    public String name;
    public Message[] params;
    public String moduleIdentity;
    private String clientIdentity;

    @Override
    public String clientIdentity() {
        return this.clientIdentity;
    }

    public ExecuteFunction(String moduleIdentity, String name, Message[] params) {
        this.name = name;
        this.params = params;
        this.moduleIdentity = moduleIdentity;
    }

    public ExecuteFunction(String moduleIdentity, String clientIdentity, String name, Message[] params) {
        this(moduleIdentity, name, params);
        this.clientIdentity = clientIdentity;
    }

    @Override
    public IModuleReceiver SetClientIdentity(String clientIdentity){
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


}
