package org.mixql.remote.messages.rtype.mtype;

import org.mixql.core.context.mtype.MType;
import org.mixql.remote.GtypeConverter;
import org.mixql.remote.messages.Message;

public interface IGtypeMessage extends Message {
    default MType toGType() throws Exception {
        return GtypeConverter.messageToGtype(this);
    }
}
