package org.mixql.protobuf.messages;

public class NULL implements Message{
    @Override
    public String toString() {
        return "{ type: " + type() + "}";
    }
    
}
