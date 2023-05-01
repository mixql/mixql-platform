package org.mixql.protobuf.messages;

public class gArray extends Message{
    public Message[] arr;

    public gArray(Message[] arr){
        this.arr = arr;
    }

    @Override
    public String type() {
        return this.getClass().getName();
    }
}
