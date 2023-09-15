package org.mixql.remote.messages.type.gtype;

import org.mixql.core.context.gtype.none;

public class NONE implements IGtypeMessage {
    @Override
    public String toString() {
        return "{ type: " + type() + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NONE)
            return true;
        else
            return false;
    }

}
