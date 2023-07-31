package org.mixql.remote.messages.module;

import org.mixql.remote.messages.Message;

public class EngineName implements Message {
    public String name;
    public EngineName(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + "}";
    }
}
