package org.mixql.remote.messages.client;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;

public class Execute implements IModuleReceiver {
    public String statement;
    private String moduleIdentity;

    private String clientIdentity;

    public Execute(String moduleIdentity, String statement) {
        this.statement = statement;
        this.moduleIdentity = moduleIdentity;
    }

    public Execute(String moduleIdentity, String clientIdentity, String statement) {
        this(moduleIdentity, statement);
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

    @Override
    public String clientIdentity() {
        return this.clientIdentity;
    }
}
