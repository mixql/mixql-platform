package org.mixql.protobuf.messages;

public class Execute implements Message{
    public String statement;

    public Execute(String statement){
        this.statement = statement;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + "statement: " + statement + "}";
    }
}
