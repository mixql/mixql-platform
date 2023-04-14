package org.mixql.protobuf;

import com.google.protobuf.GeneratedMessageV3;
import org.mixql.protobuf.generated.messages.*;
import org.mixql.protobuf.generated.messages.AnyMsg;
import org.mixql.protobuf.generated.messages.Error;

public class ProtoBufConverter {
    public static GeneratedMessageV3 unpackAnyMsg(byte[] array) {
        try {
            AnyMsg anyMsg = AnyMsg.parseFrom(array);
            switch (anyMsg.getType()) {
                case "org.mixql.protobuf.generated.messages.EngineName":
                    return anyMsg.getMsg().unpack(EngineName.getDefaultInstance().getClass());
                case "org.mixql.protobuf.generated.messages.ShutDown":
                    return anyMsg.getMsg().unpack(ShutDown.getDefaultInstance().getClass());
                case "org.mixql.protobuf.generated.messages.Execute":
                    return anyMsg.getMsg().unpack(Execute.getDefaultInstance().getClass());
                case "org.mixql.protobuf.generated.messages.Param" :
                    return anyMsg.getMsg().unpack(Param.getDefaultInstance().getClass());
                case "org.mixql.protobuf.generated.messages.Error" :
                    return anyMsg.getMsg().unpack(Error.getDefaultInstance().getClass());
                case "org.mixql.protobuf.generated.messages.SetParam" :
                    return anyMsg.getMsg().unpack(SetParam.getDefaultInstance().getClass());
                case "org.mixql.protobuf.generated.messages.GetParam" :
                    return anyMsg.getMsg().unpack(GetParam.getDefaultInstance().getClass());
                case "org.mixql.protobuf.generated.messages.IsParam" :
                    return anyMsg.getMsg().unpack(IsParam.getDefaultInstance().getClass());
                case "org.mixql.protobuf.generated.messages.ParamWasSet":
                    return anyMsg.getMsg().unpack(ParamWasSet.getDefaultInstance().getClass());
                case "org.mixql.protobuf.generated.messages.ExecuteFunction":
                    return anyMsg.getMsg().unpack(ExecuteFunction.getDefaultInstance().getClass());
                case "org.mixql.protobuf.generated.messages.GetDefinedFunctions":
                    return anyMsg.getMsg().unpack(GetDefinedFunctions.getDefaultInstance().getClass());
                case "org.mixql.protobuf.generated.messages.DefinedFunctions":
                    return anyMsg.getMsg().unpack(DefinedFunctions.getDefaultInstance().getClass());
                default:
                    return GtypeConverter
                        .toGeneratedMsg(GtypeConverter.protobufAnyToGtype(anyMsg.getMsg()));
//                case _:
//                    scala.Any =>
//                    messages
//                            .Error
//                            .newBuilder()
//                            .setMsg(
//                                    s"Protobuf any msg converter: Error: Got unknown type ${anyMsg.getType} of message"
//                            )
//                            .build()
            }
        } catch (Exception e) {
            return Error.newBuilder()
                    .setMsg("Protobuf anymsg converter: Error: " + e.getMessage())
                    .build();
        }
    }

    public static byte[] toArray(GeneratedMessageV3 msg) {
        return AnyMsg
                .newBuilder()
                .setType(msg.getClass().getName())
                .setMsg(com.google.protobuf.Any.pack(msg))
                .build()
                .toByteArray();
    }
}
