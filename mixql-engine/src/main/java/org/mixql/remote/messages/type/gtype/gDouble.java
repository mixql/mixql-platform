package org.mixql.remote.messages.type.gtype;


public class gDouble implements IGtypeMessage {
    public double value;

    public gDouble(double value){
        this.value = value;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + " value: " + value + "}";
    }
}
