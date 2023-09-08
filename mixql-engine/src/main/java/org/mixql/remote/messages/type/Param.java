package org.mixql.remote.messages.type;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;

public class Param implements Message {
    public String name;
    public Message msg;

    public Param(String name, Message msg){
        this.name = name;
        this.msg = msg;
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
