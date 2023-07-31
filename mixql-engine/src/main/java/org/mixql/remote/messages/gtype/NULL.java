package org.mixql.remote.messages.gtype;

import org.mixql.remote.messages.Message;

public class NULL implements Message {
    @Override
    public String toString() {
        return "{ type: " + type() + "}";
    }
    
}
