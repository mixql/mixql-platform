package org.mixql.protobuf.messages;

public class gString extends Message{
    public String value;
    public String quote;

    public gString(String value, String quote){
        this.quote = quote;
        this.value = value;
    }
}
