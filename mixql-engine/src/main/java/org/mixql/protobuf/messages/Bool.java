package org.mixql.protobuf.messages;

import org.mixql.core.context.gtype.bool;

public class Bool implements Message {
    public Boolean value;

    public Bool(Boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + " value: " + Boolean.toString(value) + "}";
    }
}
