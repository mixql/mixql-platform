package org.mixql.remote.messages.rtype.mtype;

import org.mixql.remote.messages.Message;

import java.util.HashMap;
import java.util.Map;

public class MMap implements IGtypeMessage {
    Map<Message, Message> m = new HashMap<>();

    public Map<Message, Message> getMap() {
        return m;
    }

    public MMap(Map<Message, Message> m) {
        this.m = m;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        for (Map.Entry<Message, Message> entry : m.entrySet()) {
            if (entry.getKey() instanceof MString)
                buffer.append(((MString) entry.getKey()).asLiteral());
            else
                buffer.append(entry.getKey().toString());
            buffer.append(": ");
            Message value = entry.getValue();
            if (value instanceof MString)
                buffer.append(((MString) value).asLiteral());
            else
                buffer.append(value.toString());
            buffer.append(", ");
        }
        buffer.append("}");
        buffer.replace(buffer.length() - 3, buffer.length(), "}");
        return "{ type: " + type() + " map: " + buffer + "}";
    }

    @Override
    public int hashCode() {
        return m.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof MMap) {
            return m.equals(((MMap) other).m);
        }
        return false;
    }
}
