package org.mixql.protobuf.messages;

import java.util.HashMap;
import java.util.Map;

public class map implements Message {
    Map<Message, Message> m = new HashMap<>();

    public Map<Message, Message> getMap() {
        return m;
    }

    public map(Map<Message, Message> m) {
        this.m = m;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        for (Map.Entry<Message, Message> entry : m.entrySet()) {
            if (entry.getKey() instanceof gString)
                buffer.append(((gString) entry.getKey()).asLiteral());
            else
                buffer.append(entry.getKey().toString());
            buffer.append(": ");
            Message value = entry.getValue();
            if (value instanceof gString)
                buffer.append(((gString) value).asLiteral());
            else
                buffer.append(value.toString());
            buffer.append(", ");
        }
        buffer.append("}");
        buffer.replace(buffer.length() - 3, buffer.length(), "}");
        return "{ type: " + type() + " map: " + buffer + "}";
    }
}
