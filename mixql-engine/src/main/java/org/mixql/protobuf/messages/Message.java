package org.mixql.protobuf.messages;

public interface Message {
    default String type() {
        return this.getClass().getName();
    }
}
