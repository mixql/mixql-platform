package org.mixql.remote.messages.type.gtype;

public class NULL implements IGtypeMessage {
    @Override
    public String toString() {
        return "{ type: " + type() + "}";
    }
    
}
