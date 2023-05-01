package org.mixql.protobuf.messages;

public class DefinedFunctions extends Message {
    public String[] arr;

    public DefinedFunctions(String[] arr) {
        this.arr = arr;
    }

    @Override
    public String type() {
        return this.getClass().getName();
    }
}
