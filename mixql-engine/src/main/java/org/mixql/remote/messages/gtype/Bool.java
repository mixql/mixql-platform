package org.mixql.remote.messages.gtype;

import org.mixql.remote.messages.Message;

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
