package org.mixql.remote.messages;

public interface Message {
    default String type() {
        return this.getClass().getName();
    }
}
