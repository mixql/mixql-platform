package org.mixql.protobuf;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mixql.protobuf.messages.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class ProtoBufConverter {
    public static Message unpackAnyMsgFromArray(byte[] array) {
        return unpackAnyMsg(new String(array, StandardCharsets.UTF_8));
    }

    private static String[] parseStringsArray(JSONArray jsonArrObject) {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < jsonArrObject.size(); i++) {
            list.add(
                    (String) jsonArrObject.get(i)
            );
        }
        String[] arr = new String[list.size()];
        return list.toArray(arr);
    }

    private static Message[] parseMessagesArray(JSONArray jsonArrObject) throws Exception {
        ArrayList<Message> list = new ArrayList();
        for (int i = 0; i < jsonArrObject.size(); i++) {
            list.add(
                    _unpackAnyMsg((JSONObject) jsonArrObject.get(i))
            );
        }
        Message[] arr = new Message[list.size()];
        return list.toArray(arr);
    }

    private static Message _unpackAnyMsg(JSONObject anyMsgJsonObject) throws Exception {
        switch ((String) anyMsgJsonObject.get("type")) {
            case "org.mixql.protobuf.messages.EngineName":
                return new EngineName(
                        (String) anyMsgJsonObject.get("name")
                );
            case "org.mixql.protobuf.messages.ShutDown":
                return new ShutDown();
            case "org.mixql.protobuf.messages.Execute":
                return new Execute(
                        (String) anyMsgJsonObject.get("statement")
                );
            case "org.mixql.protobuf.messages.Param":
                return new Param(
                        (String) anyMsgJsonObject.get("name"),
                        _unpackAnyMsg((JSONObject) anyMsgJsonObject.get("msg"))
                );
            case "org.mixql.protobuf.messages.Error":
                return new org.mixql.protobuf.messages.Error(
                        "error while unpacking from json Error: " + anyMsgJsonObject.get("msg")
                );
            case "org.mixql.protobuf.messages.SetParam":
                return new SetParam(
                        (String) anyMsgJsonObject.get("name"),
                        _unpackAnyMsg((JSONObject) anyMsgJsonObject.get("msg"))
                );
            case "org.mixql.protobuf.messages.GetParam":
                return new GetParam(
                        (String) anyMsgJsonObject.get("name")
                );
            case "org.mixql.protobuf.messages.IsParam":
                return new IsParam(
                        (String) anyMsgJsonObject.get("name")
                );
            case "org.mixql.protobuf.messages.ParamWasSet":
                return new ParamWasSet();
            case "org.mixql.protobuf.messages.ExecuteFunction":
                return new ExecuteFunction(
                        (String) anyMsgJsonObject.get("name"),
                        parseMessagesArray((JSONArray) anyMsgJsonObject
                                .get("params")
                        )
                );
            case "org.mixql.protobuf.messages.GetDefinedFunctions":
                return new GetDefinedFunctions();
            case "org.mixql.protobuf.messages.DefinedFunctions":
                return new DefinedFunctions(
                        parseStringsArray((JSONArray) anyMsgJsonObject.get("arr"))
                );
            case "org.mixql.protobuf.messages.NULL":
                return new NULL();
            case "org.mixql.protobuf.messages.Bool":
                return new Bool(
                        Boolean.parseBoolean((String) anyMsgJsonObject.get("value"))
                );
            case "org.mixql.protobuf.messages.gInt":
                return new gInt(
                        Integer.parseInt((String) anyMsgJsonObject.get("value"))
                );
            case "org.mixql.protobuf.messages.gDouble":
                return new gDouble(
                        Double.parseDouble((String) anyMsgJsonObject.get("value"))
                );
            case "org.mixql.protobuf.messages.gString":
                return new gString(
                        (String) anyMsgJsonObject.get("value"),
                        (String) anyMsgJsonObject.get("quote")
                );
            case "org.mixql.protobuf.messages.gArray":
                return new gArray(
                        parseMessagesArray((JSONArray) anyMsgJsonObject.get("arr"))
                );
        }
        throw new Exception("_unpackAnyMsg: unknown anyMsgJsonObject" + anyMsgJsonObject);
    }

    public static Message unpackAnyMsg(String json) {

        try {
            JSONObject anyMsgJsonObject = (JSONObject) JSONValue.parseWithException(json);
            return _unpackAnyMsg(anyMsgJsonObject);
        } catch (Exception e) {
            return new org.mixql.protobuf.messages.Error(
                    String.format(
                            "Protobuf anymsg converter: Error: %s", e.getMessage()
                    )
            );
        }
    }

    public static byte[] toArray(Message msg) throws Exception {
        return toJson(msg).getBytes(StandardCharsets.UTF_8);
    }

    private static JSONObject[] _toJsonObjects(Message[] msgs) throws Exception {
        ArrayList<JSONObject> list = new ArrayList<>();
        for (Message msg : msgs) {
            list.add(
                    _toJsonObject(msg)
            );
        }
        JSONObject[] arr = new JSONObject[list.size()];
        return list.toArray(arr);
    }

    private static JSONObject _toJsonObject(Message msg) throws Exception {
        if (msg instanceof EngineName) {
            return JsonUtils.buildEngineName(msg.type(), ((EngineName) msg).name);
        }

        if (msg instanceof ShutDown) {
            return JsonUtils.buildShutDown(msg.type());
        }

        if (msg instanceof Execute) {
            return JsonUtils.buildExecute(msg.type(), ((Execute) msg).statement);
        }

        if (msg instanceof Param) {
            return JsonUtils.buildParam(msg.type(), ((Param) msg).name, _toJsonObject(((Param) msg).msg));
        }

        if (msg instanceof org.mixql.protobuf.messages.Error) {
            return JsonUtils.buildError(msg.type(), ((org.mixql.protobuf.messages.Error) msg).msg);
        }

        if (msg instanceof SetParam) {
            return JsonUtils.buildSetParam(msg.type(), ((SetParam) msg).name, _toJsonObject(((SetParam) msg).msg));
        }

        if (msg instanceof GetParam) {
            return JsonUtils.buildGetParam(msg.type(), ((GetParam) msg).name);
        }

        if (msg instanceof IsParam) {
            return JsonUtils.buildIsParam(msg.type(), ((IsParam) msg).name);
        }

        if (msg instanceof ParamWasSet) {
            return JsonUtils.buildParamWasSet(msg.type());
        }


        if (msg instanceof ExecuteFunction) {
            return JsonUtils.buildExecuteFunction(msg.type(), ((ExecuteFunction) msg).name,
                    _toJsonObjects(((ExecuteFunction) msg).params)
            );
        }

        if (msg instanceof GetDefinedFunctions) {
            return JsonUtils.buildGetDefinedFunctions(msg.type());
        }


        if (msg instanceof DefinedFunctions) {
            return JsonUtils.buildDefinedFunction(msg.type(), ((DefinedFunctions) msg).arr);
        }

        if (msg instanceof NULL) {
            return JsonUtils.buildNULL(msg.type());
        }

        if (msg instanceof Bool) {
            return JsonUtils.buildBool(msg.type(), ((Bool) msg).value);
        }

        if (msg instanceof org.mixql.protobuf.messages.gInt) {
            return JsonUtils.buildInt(msg.type(), ((org.mixql.protobuf.messages.gInt) msg).value);
        }

        if (msg instanceof org.mixql.protobuf.messages.gDouble) {
            return JsonUtils.buildDouble(msg.type(), ((org.mixql.protobuf.messages.gDouble) msg).value);
        }

        if (msg instanceof gString) {
            return JsonUtils.buildGString(msg.type(), ((gString) msg).value, ((gString) msg).quote);
        }

        if (msg instanceof gArray) {
            return JsonUtils.buildGArray(msg.type(), _toJsonObjects(((gArray) msg).arr));
        }

        throw new Exception("_toJsonObject Error. Unknown type of message " + msg);
    }

    public static String toJson(Message msg) throws Exception {
        return _toJsonObject(msg).toJSONString();
    }
}
