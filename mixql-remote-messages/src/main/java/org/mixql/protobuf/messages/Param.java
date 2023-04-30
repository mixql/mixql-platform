package org.mixql.protobuf.messages;

public class Param extends Message {
    public String name;
    public String json;

    public Param(String name, String json){
        this.name = name;
        this.json = json;
    }
}
