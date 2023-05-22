package org.mixql.protobuf.messages;

public class EngineName implements Message{
    public String name;
    public EngineName(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + "}";
    }
}
