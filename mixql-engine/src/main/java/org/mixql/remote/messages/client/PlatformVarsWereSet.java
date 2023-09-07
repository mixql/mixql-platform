package org.mixql.remote.messages.client;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.client.IWorkerSender;

import java.util.ArrayList;

public class PlatformVarsWereSet implements IWorkerSender {
    public ArrayList<String> names;

    @Override
    public String sender() {
        return _sender;
    }

    private String _sender;
    private byte[] _clientAddress;

    public PlatformVarsWereSet(String sender, ArrayList<String> names) {
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
