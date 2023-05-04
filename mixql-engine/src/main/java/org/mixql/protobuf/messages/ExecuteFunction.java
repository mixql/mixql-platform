package org.mixql.protobuf.messages;

public class ExecuteFunction extends Message{

    public String name;
    public Message[] params;
    public ExecuteFunction(String name, Message[] params){
        this.name = name;
        this.params = params;
    }

    @Override
    public String type() {
        return this.getClass().getName();
    }

}
