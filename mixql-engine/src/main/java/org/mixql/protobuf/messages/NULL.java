package org.mixql.protobuf.messages;

public class NULL extends Message{

    @Override
    public String type() {
        return this.getClass().getName();
    }
}
