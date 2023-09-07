package org.mixql.remote.messages.client;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;
import org.mixql.remote.messages.gtype.gString;

public class ExecuteFunction implements Message {

    public String name;
    public Message[] params;
    public ExecuteFunction(String name, Message[] params){
        this.name = name;
        this.params = params;
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
