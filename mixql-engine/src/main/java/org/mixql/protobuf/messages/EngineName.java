package org.mixql.protobuf.messages;

public class EngineName extends Message{
    public String name;
    public EngineName(String name){
        this.name = name;
    }

    @Override
    public String type() {
        return this.getClass().getName();
    }
}
