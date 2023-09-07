package org.mixql.remote.messages.client;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.client.IWorkerSender;

public class PlatformVarsNames implements IWorkerSender {
    public String[] names;

    @Override
    public String sender() {
        return _sender;
    }
    private String _sender;

    public PlatformVarsNames(String sender, String[] names) {
        _sender = sender;
        this.names = names;
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
