package org.mixql.protobuf.messages;

public class SetParam extends Message {
    public String name;
    public Message msg;

    public SetParam(String name, Message msg){
        this.name = name;
        this.msg = msg;
    }
    @Override
    public String type() {
        return this.getClass().getName();
    }

}
