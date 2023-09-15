package org.mixql.remote.messages.type.gtype;

import org.mixql.core.context.gtype.Type;
import org.mixql.core.context.gtype.array;
import org.mixql.core.context.gtype.bool;
import org.mixql.remote.messages.Message;

import java.util.Arrays;

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

    @Override
    public int hashCode() {
        return arr.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof gArray) {
            Message[] otherArr = ((gArray) obj).arr;
            return Arrays.equals(arr, otherArr);
        }
        return false;
    }
}
