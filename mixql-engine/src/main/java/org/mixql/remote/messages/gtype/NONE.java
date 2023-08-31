package org.mixql.remote.messages.gtype;

public class NONE implements IGtypeMessage {
    @Override
    public String toString() {
        return "{ type: " + type() + "}";
    }

}
