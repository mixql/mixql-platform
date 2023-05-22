package org.mixql.protobuf.messages;

public class DefinedFunctions implements Message {
    public String[] arr;

    public DefinedFunctions(String[] arr) {
        this.arr = arr;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + "}";
    }
}
