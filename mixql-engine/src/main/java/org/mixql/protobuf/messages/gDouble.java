package org.mixql.protobuf.messages;

public class gDouble extends Message {
    public double value;

    public gDouble(double value){
        this.value = value;
    }

    @Override
    public String type() {
        return this.getClass().getName();
    }
}
