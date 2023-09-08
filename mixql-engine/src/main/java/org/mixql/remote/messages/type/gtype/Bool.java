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
}
