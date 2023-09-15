package org.mixql.remote.messages.type.gtype;

public class NULL implements IGtypeMessage {
    @Override
    public String toString() {
        return "{ type: " + type() + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NULL)
            return true;
        else
            return false;
    }

}
