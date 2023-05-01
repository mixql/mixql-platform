package org.mixql.protobuf.messages;

public class gInt extends Message{
    public int value;
    public gInt(int value){
        this.value = value;
    }
    @Override
    public String type() {
        return this.getClass().getName();
    }
}
