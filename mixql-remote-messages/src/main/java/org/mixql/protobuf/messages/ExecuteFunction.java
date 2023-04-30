package org.mixql.protobuf.messages;

public class ExecuteFunction extends Message{

    public String name;
    public gArray params;
    public ExecuteFunction(String name, gArray params){
        this.name = name;
        this.params = params;
    }
}
