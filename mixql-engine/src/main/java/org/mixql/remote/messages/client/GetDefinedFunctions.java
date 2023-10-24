package org.mixql.remote.messages.client;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;

public class GetDefinedFunctions implements IModuleReceiver {

    public String moduleIdentity;
    private String clientIdentity;

    @Override
    public String clientIdentity() {
        return this.clientIdentity;
    }

    public GetDefinedFunctions(String moduleIdentity) {
        this.moduleIdentity = moduleIdentity;
    }

    public GetDefinedFunctions(String moduleIdentity, String clientIdentity) {
        this(moduleIdentity);
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
}