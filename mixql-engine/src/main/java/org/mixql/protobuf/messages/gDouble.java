package org.mixql.protobuf.messages;

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
