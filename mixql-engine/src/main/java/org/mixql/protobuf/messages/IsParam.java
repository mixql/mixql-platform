package org.mixql.protobuf.messages;

public class IsParam extends Message {
    public String name;

    public IsParam(String name){
        this.name = name;
    }

    @Override
    public String type() {
        return this.getClass().getName();
    }
}
