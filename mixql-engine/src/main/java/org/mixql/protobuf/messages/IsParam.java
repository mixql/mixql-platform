package org.mixql.protobuf.messages;

public class IsParam implements Message {
    public String name;

    public IsParam(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + " name: " + name + "}";
    }
    
}
