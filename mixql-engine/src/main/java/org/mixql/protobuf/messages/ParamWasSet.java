package org.mixql.protobuf.messages;

public class ParamWasSet extends Message{
    @Override
    public String type() {
        return this.getClass().getName();
    }
}
