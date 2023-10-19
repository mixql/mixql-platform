package org.mixql.remote.messages.rtype.mtype;

public class MInt implements IGtypeMessage {
    public long value;
    public MInt(long value){
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
        if (obj instanceof MInt) {
            return ((MInt) obj).value == value;
        }
        if (obj instanceof Long) {
            return (Long) obj == value;
        }
        return false;
    }

}
