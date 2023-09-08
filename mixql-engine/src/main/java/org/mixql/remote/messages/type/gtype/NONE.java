package org.mixql.remote.messages.type.gtype;

public class NONE implements IGtypeMessage {
    @Override
    public String toString() {
        return "{ type: " + type() + "}";
    }

}
