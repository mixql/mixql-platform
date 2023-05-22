package org.mixql.protobuf.messages;

public class Error implements Message{
    public String msg;

    public Error(String msg){
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + "msg: " + msg + "}";
    }
    
}
