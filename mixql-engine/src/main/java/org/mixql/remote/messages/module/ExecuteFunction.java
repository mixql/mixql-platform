package org.mixql.remote.messages.module;

import org.mixql.remote.messages.Message;
import org.mixql.remote.messages.gtype.gString;

public class ExecuteFunction implements Message {

    public String name;
    public Message[] params;
    public ExecuteFunction(String name, Message[] params){
        this.name = name;
        this.params = params;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        for (int i = 0; i <= params.length - 1; i++) {
            Message a = params[i];
            if (a instanceof gString)
                buffer.append(((gString) a).asLiteral());
            else
                buffer.append(a.toString());
            if (i != params.length - 1) {
                buffer.append(", ");
            }
        }
        buffer.append("]");
        return "{ type: " + type() + "name: " + name + "params: " + buffer + "}";
    }

}
