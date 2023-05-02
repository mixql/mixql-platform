package org.mixql.protobuf.messages;

public class GetParam extends Message {
    public String name;

    public GetParam(String name){
        this.name = name;
    }
    @Override
    public String type() {
        return this.getClass().getName();
    }

}