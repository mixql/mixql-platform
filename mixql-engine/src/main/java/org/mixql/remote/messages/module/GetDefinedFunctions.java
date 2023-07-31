package org.mixql.remote.messages.module;

import org.mixql.remote.messages.Message;

public class GetDefinedFunctions implements Message {
    @Override
    public String toString() {
        return "{ type: " + type() + "}";
    }
}