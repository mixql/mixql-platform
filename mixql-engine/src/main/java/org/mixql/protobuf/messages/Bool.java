package org.mixql.protobuf.messages;

public class Bool extends Message {
    public Boolean value;
    public Bool(Boolean value){
        this.value = value;
    }

    @Override
    public String type() {
        return this.getClass().getName();
    }
}
