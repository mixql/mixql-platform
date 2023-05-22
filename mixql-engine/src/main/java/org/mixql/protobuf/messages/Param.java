package org.mixql.protobuf.messages;

public class Param implements Message {
    public String name;
    public Message msg;

    public Param(String name, Message msg){
        this.name = name;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + " mame: " + name + " msg: " + msg + "}";
    }
    
}
