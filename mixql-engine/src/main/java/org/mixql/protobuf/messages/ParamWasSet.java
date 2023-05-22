package org.mixql.protobuf.messages;

public class ParamWasSet implements Message{
    @Override
    public String toString() {
        return "{ type: " + type() + "}";
    }
}
