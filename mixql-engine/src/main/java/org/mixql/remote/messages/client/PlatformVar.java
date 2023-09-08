package org.mixql.remote.messages.client;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;

public class PlatformVar implements IWorkerReceiver {
    public String name;
    public Message msg;

    private String _sender;

    public String moduleIdentity;

    @Override
    public String workerIdentity() {
        return _sender;
    }

    public PlatformVar(String moduleIdentity, String sender, String key, Message msg) {
        _sender = sender;
        this.name = key;
        this.msg = msg;
        this.moduleIdentity = moduleIdentity;
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
