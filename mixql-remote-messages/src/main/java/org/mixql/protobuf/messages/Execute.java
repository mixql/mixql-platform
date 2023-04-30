package org.mixql.protobuf.messages;

public class Execute extends Message{
    public String statement;

    public Execute(String statement){
        this.statement = statement;
    }
}
