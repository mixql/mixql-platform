package org.mixql.remote.messages.gtype;

import org.mixql.remote.messages.Message;

public class gDouble implements Message {
    public double value;

    public gDouble(double value){
        this.value = value;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + " value: " + value + "}";
    }
}
