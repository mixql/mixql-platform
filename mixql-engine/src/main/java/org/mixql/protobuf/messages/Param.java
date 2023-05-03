package org.mixql.protobuf.messages;

public class Param extends Message {
    public String name;
    public Message msg;

    public Param(String name, Message msg){
        this.name = name;
        this.msg = msg;
    }

    @Override
    public String type() {
        return this.getClass().getName();
    }
}
