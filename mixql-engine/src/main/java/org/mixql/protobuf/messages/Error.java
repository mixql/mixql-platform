package org.mixql.protobuf.messages;

public class Error extends Message{
    public String msg;

    public Error(String msg){
        this.msg = msg;
    }

    @Override
    public String type() {
        return this.getClass().getName();
    }
}
