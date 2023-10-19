package org.mixql.remote.messages.rtype.mtype;

public class MNONE implements IGtypeMessage {
    @Override
    public String toString() {
        return "{ type: " + type() + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MNONE)
            return true;
        else
            return false;
    }

}
