package org.mixql.remote.messages.gtype;

import org.mixql.remote.messages.Message;

public class gInt implements Message {
    public int value;
    public gInt(int value){
        this.value = value;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + " value: " + value + "}";
    }

}
