package org.mixql.remote.messages.gtype;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;

public class Error implements Message {
    public String msg;

    public Error(String msg){
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
