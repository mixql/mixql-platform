package org.mixql.protobuf.messages;

public class gString implements Message{
    public String value;
    public String quote;

    public gString(String value, String quote){
        this.quote = quote;
        this.value = value;
    }

    public String asLiteral() {
        String q = "\"";
        if (quote != "") q = quote;
        return q + value + q;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + " value: " + value + "quote: " + quote + "}";
    }
}
