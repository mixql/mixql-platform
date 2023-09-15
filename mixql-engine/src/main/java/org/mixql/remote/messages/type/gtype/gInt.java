package org.mixql.remote.messages.type.gtype;

public class gInt implements IGtypeMessage {
    public long value;
    public gInt(long value){
        this.value = value;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + " value: " + value + "}";
    }

    @Override
    public int hashCode() {
        return Long.valueOf(value).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof gInt) {
            return ((gInt) obj).value == value;
        }
        if (obj instanceof Long) {
            return (Long) obj == value;
        }
        return false;
    }

}
