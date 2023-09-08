package org.mixql.remote.messages;

import org.mixql.remote.RemoteMessageConverter;

public interface Message {
    default String type() {
        return this.getClass().getName();
    }

    default byte[] toByteArray() throws Exception {
        return RemoteMessageConverter.toArray(this);
    }
}
