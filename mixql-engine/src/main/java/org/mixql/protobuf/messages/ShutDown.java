package org.mixql.protobuf.messages;

public class ShutDown implements Message{
    @Override
    public String toString() {
        return "{ type: " + type() + "}";
    }
}
