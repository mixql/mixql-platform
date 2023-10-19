package org.mixql.remote.messages.rtype.mtype;


public class MDouble implements IGtypeMessage {
    public double value;

    public MDouble(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + " value: " + value + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MDouble) {
            if (Math.abs(value - ((MDouble) obj).value) <= 0.000001)
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
