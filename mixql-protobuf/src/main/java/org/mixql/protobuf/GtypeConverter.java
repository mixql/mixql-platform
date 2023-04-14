package org.mixql.protobuf;

import com.google.protobuf.Any;
import org.mixql.core.context.gtype.*;
import org.mixql.protobuf.generated.messages.*;
import org.mixql.protobuf.generated.messages.Double;
import org.mixql.protobuf.generated.messages.Error;
import org.mixql.protobuf.generated.messages.String;

import java.util.*;

public class GtypeConverter {

    public static Type toGtype(com.google.protobuf.GeneratedMessageV3 remoteMsg) throws Exception {
        if (remoteMsg instanceof NULL)
            return new Null();

        if (remoteMsg instanceof Int)
            return new gInt(((Int) remoteMsg).getValue());

        if (remoteMsg instanceof Double)
            return new gDouble(((Double) remoteMsg).getValue());

        if (remoteMsg instanceof Bool)
            return new bool(((Bool) remoteMsg).getValue());

        if (remoteMsg instanceof String) {
            String msg = (String) remoteMsg;
            return new string(msg.getValue(), msg.getQuote());
        }

        if (remoteMsg instanceof Array) {
            List<Any> anyList = ((Array) remoteMsg).getArrList();
            Type[] gtypeList = new Type[anyList.size()];

            int i = 0;
            for (Any anyValue : anyList) {
                gtypeList[i++] = protobufAnyToGtype(anyValue);
            }

            return new array(gtypeList);
        }

        if (remoteMsg instanceof Error)
            throw new Exception(((Error) remoteMsg).getMsg());

        throw new Exception(
                java.lang.String.format(
                        "RemoteMsgsConverter: toGtype error: " +
                                "got unknown message %s", remoteMsg.getClass().getName()
                )
        );
    }

    public static Type protobufAnyToGtype(com.google.protobuf.Any f) throws Exception {
        System.out.println("mixql-protobuf: protobufAnyToGtype");
        if (f.is(NULL.getDefaultInstance().getClass()))
            return new Null();

        if (f.is(Bool.getDefaultInstance().getClass())) {
            Bool msg = f.unpack(Bool.getDefaultInstance().getClass());
            return new bool(msg.getValue());
        }

        if (f.is(Int.getDefaultInstance().getClass())) {
            Int msg = f.unpack(Int.getDefaultInstance().getClass());
            return new gInt(msg.getValue());
        }

        if (f.is(Double.getDefaultInstance().getClass())) {
            Double msg = f.unpack(Double.getDefaultInstance().getClass());
            return new gDouble(msg.getValue());
        }

        if (f.is(String.getDefaultInstance().getClass())) {
            String msg = f.unpack(String.getDefaultInstance().getClass());
            return new string(msg.getValue(), msg.getQuote());
        }


        if (f.is(Array.getDefaultInstance().getClass())) {
            Array msg = f.unpack(Array.getDefaultInstance().getClass());
            return toGtype(msg);
        }

        throw new Exception(
                java.lang.String.format(
                        "protobufAnyToGtype: error: could not convert " +
                                "com.google.protobuf.any.Any to gtype: got %s, when gtype was expected",
                        f.getClass().getName()
                )
        );
    }

    public static com.google.protobuf.GeneratedMessageV3 toGeneratedMsg(Type gValue) throws Exception {
        System.out.println("mixql-protobuf: toGeneratedMsg");
        if (gValue instanceof Null)
            return NULL.newBuilder().getDefaultInstanceForType();

        if (gValue instanceof bool)
            return Bool.newBuilder().setValue(((bool) gValue).getValue()).build();

        if (gValue instanceof gInt)
            return Int.newBuilder().setValue(((gInt) gValue).getValue()).build();

        if (gValue instanceof gDouble)
            return Double.newBuilder().setValue(((gDouble) gValue).getValue()).build();

        if (gValue instanceof string) {
            string msg = (string) gValue;
            return String.newBuilder().setValue(msg.getValue()).setQuote(msg.getQuote()).build();
        }

        if (gValue instanceof array) {
            Type[] gtypeArr = ((array) gValue).getArr();
            List<Any> anyValueList = new ArrayList<>();

            for (Type gType : gtypeArr) {
                anyValueList.add(Any.pack(toGeneratedMsg(gType)));
            }
            return Array.newBuilder().addAllArr(anyValueList).build();
        }

        throw new Exception(
                java.lang.String.format(
                        "toGeneratedMsg: error: could not convert " +
                                "gtype to com.google.protobuf.any.Any: got unknown %s",
                        gValue.getClass().getName()
                )
        );
    }

    public static Any toProtobufAny(com.google.protobuf.GeneratedMessageV3 remoteMsg) throws Exception {
        if (remoteMsg instanceof NULL)
            return Any.pack(NULL.getDefaultInstance());

        if (remoteMsg instanceof Bool)
            return Any.pack((Bool) remoteMsg);

        if (remoteMsg instanceof Int)
            return Any.pack((Int) remoteMsg);

        if (remoteMsg instanceof Double)
            return Any.pack((Double) remoteMsg);

        if (remoteMsg instanceof String)
            return Any.pack((String) remoteMsg);

        if (remoteMsg instanceof Array)
            return Any.pack((Array) remoteMsg);

        if (remoteMsg instanceof Error)
            throw new Exception(((Error) remoteMsg).getMsg());


        throw new Exception(
                java.lang.String.format(
                        "RemoteMsgsConverter: toProtobufAny error: " +
                                "got %s, when type in GeneratedMessageV3 format was expected",
                        remoteMsg.getClass().getName()
                )
        );
    }

    public static com.google.protobuf.Any toProtobufAny(Type gValue) throws Exception {
        if (gValue instanceof Null)
            return Any.pack(NULL.getDefaultInstance());

        if (gValue instanceof bool)
            return Any.pack(Bool.newBuilder().setValue(((bool) gValue).getValue()).build());

        if (gValue instanceof gInt)
            return Any.pack(Int.newBuilder().setValue(((gInt) gValue).getValue()).build());

        if (gValue instanceof gDouble)
            return Any.pack(Double.newBuilder().setValue(((gDouble) gValue).getValue()).build());

        if (gValue instanceof string) {
            string msg = (string) gValue;
            return com.google.protobuf.Any.pack(String.newBuilder()
                    .setValue(msg.getValue()).setQuote(msg.getQuote()).build()
            );
        }

        if (gValue instanceof array) {
            Type[] gtypeArr = ((array) gValue).getArr();
            List<Any> anyValueList = new ArrayList<>();

            for (Type gType : gtypeArr) {
                anyValueList.add(Any.pack(toGeneratedMsg(gType)));
            }

            return Any.pack(Array.newBuilder().addAllArr(anyValueList).build());
        }

        throw new Exception(
                java.lang.String.format(
                        "RemoteMsgsConverter: toProtobufAny error: " +
                                "got %s, when type in gValue format was expected",
                        gValue.getClass().getName()
                )
        );

    }
}
