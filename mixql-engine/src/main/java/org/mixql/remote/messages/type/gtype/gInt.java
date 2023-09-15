package org.mixql.remote.messages.type.gtype;

public class gInt implements IGtypeMessage {
    public int value;
    public gInt(int value){
        this.value = value;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + " value: " + value + "}";
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(value).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof gInt) {
            return ((gInt) obj).value == value;
        }
        if (obj instanceof Integer) {
            return (Integer) obj == value;
        }
        return false;
    }

}
