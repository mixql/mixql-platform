package org.mixql.remote.messages.type.gtype;

public class Bool implements IGtypeMessage {
    public Boolean value;

    public Bool(Boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + " value: " + Boolean.toString(value) + "}";
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Bool) {
            int res = ((Bool) obj).value.compareTo(value);
            if (res == 0)
                return true;
            else
                return false;
        }
        if (obj instanceof Boolean) {
            int res = ((Boolean) obj).compareTo(value);
            if (res == 0)
                return true;
            else
                return false;
        }
        return false;
    }
}
