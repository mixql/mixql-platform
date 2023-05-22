package org.mixql.protobuf.messages;

public class GetParam implements Message {
    public String name;

    public GetParam(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + " name: " + name + "}";
    }
}
