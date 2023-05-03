package org.mixql.protobuf.messages;

public class ShutDown extends Message{

    @Override
    public String type() {
        return this.getClass().getName();
    }
}
