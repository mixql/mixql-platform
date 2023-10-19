package org.mixql.remote.messages.rtype.mtype;

public class MString implements IGtypeMessage {
    public String value;
    public String quote;

    public MString(String value, String quote){
        this.quote = quote;
        this.value = value;
    }

    public String asLiteral() {
        String q = "\"";
        if (quote != "") q = quote;
        return q + value + q;
    }

    public String quoted() {
        return quote + value + quote;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + " value: " + value + "quote: " + quote + "}";
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MString) {
            int res = ((MString) obj).value.compareTo(value);
            if (res == 0)
                return true;
            else
                return false;
        }
        if (obj instanceof String) {
            int res = ((String) obj).compareTo(value);
            if (res == 0)
                return true;
            else
                return false;
        }
        return false;
    }
}
