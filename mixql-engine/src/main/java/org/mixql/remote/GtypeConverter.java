package org.mixql.remote;

import org.mixql.core.context.gtype.*;
import org.mixql.core.context.gtype.map;
import org.mixql.remote.messages.*;
import org.mixql.remote.messages.module.Error;
import org.mixql.remote.messages.gtype.Bool;
import org.mixql.remote.messages.gtype.NULL;
import org.mixql.remote.messages.gtype.gArray;
import org.mixql.remote.messages.gtype.gString;

import java.util.HashMap;

public class GtypeConverter {

    public static Type[] messagesToGtypes(Message[] remoteMsgs) throws Exception {
        Type[] gSeq = new Type[remoteMsgs.length];
        for (int i = 0; i < remoteMsgs.length; i++) {
            gSeq[i] = messageToGtype(remoteMsgs[i]);
        }
        return gSeq;
    }

    public static Type messageToGtype(Message msg) throws Exception {
        if (msg instanceof NULL)
            return new Null();

        if (msg instanceof Bool)
            return new bool(((Bool) msg).value);

        if (msg instanceof org.mixql.remote.messages.gtype.gInt)
            return new org.mixql.core.context.gtype.gInt(((org.mixql.remote.messages.gtype.gInt) msg).value);

        if (msg instanceof org.mixql.remote.messages.gtype.gDouble)
            return new org.mixql.core.context.gtype.gDouble(((org.mixql.remote.messages.gtype.gDouble) msg).value);

        if (msg instanceof gString)
            return new string(((gString) msg).value);

        if (msg instanceof gArray)
            return new array(messagesToGtypes(((gArray) msg).arr));

        if (msg instanceof Error)
            throw new Exception(((Error) msg).msg);

        if (msg instanceof org.mixql.remote.messages.gtype.map) {
            HashMap<Type, Type> m = new HashMap<>();
            org.mixql.remote.messages.gtype.map msgMap = (org.mixql.remote.messages.gtype.map) msg;

            for (Message key : msgMap.getMap().keySet()) {
                m.put(messageToGtype(key), messageToGtype(msgMap.getMap().get(key)));
            }

            return new map(m);
        }

        throw new Exception(
                String.format(
                        "RemoteMsgsConverter: toGtype error: " +
                                "got %s, when type was expected",
                        msg.toString()
                )
        );
    }

    public static Message[] toGeneratedMsgs(Type[] gValues) throws Exception {
        Message[] gSeq = new Message[gValues.length];
        for (int i = 0; i < gValues.length; i++) {
            gSeq[i] = toGeneratedMsg(gValues[i]);
        }
        return gSeq;
    }


    public static Message toGeneratedMsg(Type gValue) throws Exception {
        if (gValue instanceof Null)
            return new NULL();

        if (gValue instanceof bool)
            return new Bool(((bool) gValue).getValue());

        if (gValue instanceof org.mixql.core.context.gtype.gInt)
            return new org.mixql.remote.messages.gtype.gInt(
                    ((org.mixql.core.context.gtype.gInt) gValue).getValue()
            );

        if (gValue instanceof org.mixql.core.context.gtype.gDouble)
            return new org.mixql.remote.messages.gtype.gDouble(
                    ((org.mixql.core.context.gtype.gDouble) gValue).getValue()
            );

        if (gValue instanceof string)
            return new gString(
                    ((string) gValue).getValue(),
                    ((string) gValue).getQuote()
            );

        if (gValue instanceof array)
            return new gArray(
                    toGeneratedMsgs(((array) gValue).getArr())
            );

        if (gValue instanceof map) {
            HashMap<Message, Message> m = new HashMap<>();
            map gMap = (map) gValue;

            for (Type key : gMap.getMap().keySet()) {
                m.put(toGeneratedMsg(key), toGeneratedMsg(gMap.getMap().get(key)));
            }
            return new org.mixql.remote.messages.gtype.map(m);
        }

        throw new Exception("toGeneratedMsg Error!! Unknown gValue was provided: " + gValue.toString());
    }
}
