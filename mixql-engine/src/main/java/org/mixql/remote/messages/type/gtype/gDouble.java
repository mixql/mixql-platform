package org.mixql.remote.messages.type.gtype;


public class gDouble implements IGtypeMessage {
    public double value;

    public gDouble(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + " value: " + value + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof gDouble) {
            if (Math.abs(value - ((gDouble) obj).value) <= 0.000001)
                return true;
            else
                return false;
        }
        if (obj instanceof Double) {
            if (Math.abs(value - (Double) obj) <= 0.000001)
                return true;
            else
                return false;
        }
        return false;
    }
}
