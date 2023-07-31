package org.mixql.remote.messages.gtype;

public class NULL implements IGtypeMessage {
    @Override
    public String toString() {
        return "{ type: " + type() + "}";
    }
    
}
