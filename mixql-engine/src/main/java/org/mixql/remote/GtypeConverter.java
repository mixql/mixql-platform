package org.mixql.remote;

import org.mixql.core.context.mtype.*;
import org.mixql.remote.messages.*;
import org.mixql.remote.messages.rtype.mtype.*;
import org.mixql.remote.messages.rtype.Error;
import org.mixql.remote.messages.rtype.mtype.MArray;
import org.mixql.remote.messages.rtype.mtype.MBool;
import org.mixql.remote.messages.rtype.mtype.MDouble;
import org.mixql.remote.messages.rtype.mtype.MInt;
import org.mixql.remote.messages.rtype.mtype.MMap;
import org.mixql.remote.messages.rtype.mtype.MString;

import java.util.HashMap;

public class GtypeConverter {

    public static MType[] messagesToGtypes(Message[] remoteMsgs) throws Exception {
        MType[] gSeq = new MType[remoteMsgs.length];
        for (int i = 0; i < remoteMsgs.length; i++) {
            gSeq[i] = messageToGtype(remoteMsgs[i]);
        }
        return gSeq;
    }

    public static MType messageToGtype(Message msg) throws Exception {
        if (msg instanceof MNULL)
            return MNull.get();

        if (msg instanceof MNONE)
            return MNone.get();

        if (msg instanceof MBool)
            return new org.mixql.core.context.mtype.MBool(((MBool) msg).value);

        if (msg instanceof MInt)
            return new org.mixql.core.context.mtype.MInt(((MInt) msg).value);

        if (msg instanceof MDouble)
            return new org.mixql.core.context.mtype.MDouble(((MDouble) msg).value);

        if (msg instanceof MString)
            return new org.mixql.core.context.mtype.MString(((MString) msg).value, ((MString) msg).quote);

        if (msg instanceof MArray)
            return new org.mixql.core.context.mtype.MArray(messagesToGtypes(((MArray) msg).arr));

        if (msg instanceof Error)
            throw new Exception(((Error) msg).getErrorMessage());

        if (msg instanceof MMap) {
            HashMap<MType, MType> m = new HashMap<>();
            MMap msgMap = (MMap) msg;

            for (Message key : msgMap.getMap().keySet()) {
                m.put(messageToGtype(key), messageToGtype(msgMap.getMap().get(key)));
            }

            return new org.mixql.core.context.mtype.MMap(m);
        }

        throw new Exception(
                String.format(
                        "RemoteMsgsConverter: toGtype error: " +
                                "got %s, when type was expected",
                        msg.toString()
                )
        );
    }

    public static Message[] toGeneratedMsgs(MType[] gValues) throws Exception {
        Message[] gSeq = new Message[gValues.length];
        for (int i = 0; i < gValues.length; i++) {
            gSeq[i] = toGeneratedMsg(gValues[i]);
        }
        return gSeq;
    }


    public static Message toGeneratedMsg(MType mValue) throws Exception {
        if (mValue instanceof org.mixql.core.context.mtype.MNull)
            return new MNULL();

        if (mValue instanceof org.mixql.core.context.mtype.MNone)
            return new MNONE();

        if (mValue instanceof org.mixql.core.context.mtype.MBool)
            return new MBool(((org.mixql.core.context.mtype.MBool) mValue).getValue());

        if (mValue instanceof org.mixql.core.context.mtype.MInt)
            return new MInt(
                    ((org.mixql.core.context.mtype.MInt) mValue).getValue()
            );

        if (mValue instanceof org.mixql.core.context.mtype.MDouble)
            return new MDouble(
                    ((org.mixql.core.context.mtype.MDouble) mValue).getValue()
            );

        if (mValue instanceof org.mixql.core.context.mtype.MString)
            return new MString(
                    ((org.mixql.core.context.mtype.MString) mValue).getValue(),
                    ((org.mixql.core.context.mtype.MString) mValue).getQuote()
            );

        if (mValue instanceof org.mixql.core.context.mtype.MArray)
            return new MArray(
                    toGeneratedMsgs(((org.mixql.core.context.mtype.MArray) mValue).getArr())
            );

        if (mValue instanceof org.mixql.core.context.mtype.MMap) {
            HashMap<Message, Message> m = new HashMap<>();
            org.mixql.core.context.mtype.MMap gMap = (org.mixql.core.context.mtype.MMap) mValue;

            for (MType key : gMap.getMap().keySet()) {
                m.put(toGeneratedMsg(key), toGeneratedMsg(gMap.getMap().get(key)));
            }
            return new MMap(m);
        }

        throw new Exception("toGeneratedMsg Error!! Unknown gValue was provided: " + mValue.toString());
    }
}
