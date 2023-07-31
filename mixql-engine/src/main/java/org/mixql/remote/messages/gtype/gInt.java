package org.mixql.remote.messages.gtype;

public class gInt implements IGtypeMessage {
    public int value;
    public gInt(int value){
        this.value = value;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + " value: " + value + "}";
    }

}
