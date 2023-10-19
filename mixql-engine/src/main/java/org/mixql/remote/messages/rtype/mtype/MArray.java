package org.mixql.remote.messages.rtype.mtype;

import org.mixql.remote.messages.Message;

import java.util.Arrays;

public class MArray implements IGtypeMessage {
    public Message[] arr;

    public MArray(Message[] arr){
        this.arr = arr;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        for (int i = 0; i <= arr.length - 1; i++) {
            Message a = arr[i];
            if (a instanceof MString)
                buffer.append(((MString) a).asLiteral());
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
        if (obj instanceof MArray) {
            Message[] otherArr = ((MArray) obj).arr;
            return Arrays.equals(arr, otherArr);
        }
        return false;
    }
}
