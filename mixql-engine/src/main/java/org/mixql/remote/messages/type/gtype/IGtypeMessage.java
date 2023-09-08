package org.mixql.remote.messages.type.gtype;

import org.mixql.core.context.gtype.Type;
import org.mixql.remote.GtypeConverter;
import org.mixql.remote.messages.Message;

public interface IGtypeMessage extends Message {
    default Type toGType() throws Exception {
        return GtypeConverter.messageToGtype(this);
    }
}
