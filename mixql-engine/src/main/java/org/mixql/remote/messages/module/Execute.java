package org.mixql.remote.messages.module;

import org.mixql.remote.messages.Message;

public class Execute implements Message {
    public String statement;

    public Execute(String statement){
        this.statement = statement;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + "statement: " + statement + "}";
    }
}
