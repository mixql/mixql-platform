package org.mixql.protobuf.messages;

public class gArray implements Message{
    public Message[] arr;

    public gArray(Message[] arr){
        this.arr = arr;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        for (int i = 0; i <= arr.length - 1; i++) {
            Message a = arr[i];
            if (a instanceof gString)
                buffer.append(((gString) a).asLiteral());
            else
                buffer.append(a.toString());
            if (i != arr.length - 1) {
                buffer.append(", ");
            }
        }
        buffer.append("]");
        return "{ type: " + type() + " arr: " + buffer + "}";
    }
}
