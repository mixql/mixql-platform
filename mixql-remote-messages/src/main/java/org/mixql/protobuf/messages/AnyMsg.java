package org.mixql.protobuf.messages;

public class AnyMsg extends Message{
    public String type;
    public String json;
    public AnyMsg(String type, String json){
        this.type = type;
        this.json = json;
    }
}
