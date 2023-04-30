package org.mixql.protobuf.messages;

public class SetParam extends Message {
    public String name;
    public String json;

    public SetParam(String name, String json){
        this.name = name;
        this.json = json;
    }
}
