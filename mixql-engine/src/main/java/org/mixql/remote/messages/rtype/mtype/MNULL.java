package org.mixql.remote.messages.rtype.mtype;

public class MNULL implements IGtypeMessage {
    @Override
    public String toString() {
        return "{ type: " + type() + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MNULL)
            return true;
        else
            return false;
    }

}
