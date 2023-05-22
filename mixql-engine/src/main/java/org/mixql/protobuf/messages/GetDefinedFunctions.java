package org.mixql.protobuf.messages;

public class GetDefinedFunctions implements Message {
    @Override
    public String toString() {
        return "{ type: " + type() + "}";
    }
}