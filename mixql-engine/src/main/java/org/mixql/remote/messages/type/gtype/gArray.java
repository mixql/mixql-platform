package org.mixql.remote.messages.type.gtype;

import org.mixql.remote.messages.Message;

public class gArray implements IGtypeMessage {
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
