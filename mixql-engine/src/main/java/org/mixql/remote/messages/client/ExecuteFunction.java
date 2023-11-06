package org.mixql.remote.messages.client;

import org.mixql.core.context.mtype.MType;
import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;

import java.util.HashMap;
import java.util.Map;

public class ExecuteFunction implements IModuleReceiver {

    public String name;
    public Message[] params;
    public String moduleIdentity;
    private String clientIdentity;
    Map<String, Message> kwargs = new HashMap<>();

    public Map<String, Message> getKwargs(){
        return kwargs;
    }

    @Override
    public String clientIdentity() {
        return this.clientIdentity;
    }

    public ExecuteFunction(String moduleIdentity, String name, Message[] params, Map<String, Message> kwargs) {
        this.name = name;
        this.params = params;
        this.moduleIdentity = moduleIdentity;
        this.kwargs = kwargs;
    }

    public ExecuteFunction(String moduleIdentity, String clientIdentity, String name, Message[] params,
                           Map<String, Message> kwargs) {
        this(moduleIdentity, name, params, kwargs);
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
