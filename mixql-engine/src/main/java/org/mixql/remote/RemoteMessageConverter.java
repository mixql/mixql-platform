package org.mixql.remote;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mixql.remote.messages.*;
import org.mixql.remote.messages.client.*;
import org.mixql.remote.messages.client.toBroker.EngineStarted;
import org.mixql.remote.messages.module.*;
import org.mixql.remote.messages.type.gtype.*;
import org.mixql.remote.messages.type.Param;
import org.mixql.remote.messages.module.worker.*;
import org.mixql.remote.messages.type.Error;

import java.nio.charset.StandardCharsets;
import java.util.*;


public class RemoteMessageConverter {
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
            case "org.mixql.remote.messages.client.ShutDown":
                return new ShutDown();
            case "org.mixql.remote.messages.client.Execute":
                return new Execute(
                        (String) anyMsgJsonObject.get("moduleIdentity"),
                        (String) anyMsgJsonObject.get("statement")
                );
            case "org.mixql.remote.messages.type.Param":
                return new Param(
                        (String) anyMsgJsonObject.get("name"),
                        _unpackAnyMsg((JSONObject) anyMsgJsonObject.get("msg"))
                );
            case "org.mixql.remote.messages.type.Error":
                return new Error(
                        "error while unpacking from json Error: " + anyMsgJsonObject.get("msg")
                );
            case "org.mixql.remote.messages.client.ExecuteFunction":
                return new ExecuteFunction(
                        (String) anyMsgJsonObject.get("name"),
                        parseMessagesArray((JSONArray) anyMsgJsonObject
                                .get("params")
                        )
                );
            case "org.mixql.remote.messages.client.GetDefinedFunctions":
                return new GetDefinedFunctions();
            case "org.mixql.remote.messages.module.DefinedFunctions":
                return new DefinedFunctions(
                        parseStringsArray((JSONArray) anyMsgJsonObject.get("arr"))
                );
            case "org.mixql.remote.messages.type.gtype.NULL":
                return new NULL();
            case "org.mixql.remote.messages.type.gtype.NONE":
                return new NONE();
            case "org.mixql.remote.messages.type.gtype.Bool":
                return new Bool(
                        Boolean.parseBoolean((String) anyMsgJsonObject.get("value"))
                );
            case "org.mixql.remote.messages.type.gtype.gInt":
                return new gInt(
                        Integer.parseInt((String) anyMsgJsonObject.get("value"))
                );
            case "org.mixql.remote.messages.type.gtype.gDouble":
                return new gDouble(
                        Double.parseDouble((String) anyMsgJsonObject.get("value"))
                );
            case "org.mixql.remote.messages.type.gtype.gString":
                return new gString(
                        (String) anyMsgJsonObject.get("value"),
                        (String) anyMsgJsonObject.get("quote")
                );
            case "org.mixql.remote.messages.type.gtype.gArray":
                return new gArray(
                        parseMessagesArray((JSONArray) anyMsgJsonObject.get("arr"))
                );
            case "org.mixql.remote.messages.type.gtype.map":
                JSONArray mapJsonObject = (JSONArray) anyMsgJsonObject.get("map");
                Map<Message, Message> m = new HashMap<>();
                for (int i = 0; i < mapJsonObject.size(); i++) {
                    m.put(_unpackAnyMsg(
                                    (JSONObject) ((JSONObject) mapJsonObject.get(i)).get("key")
                            ),
                            _unpackAnyMsg(
                                    (JSONObject) ((JSONObject) mapJsonObject.get(i)).get("value")
                            )
                    );
                }
                return new map(m);
            case "org.mixql.remote.messages.module.worker.GetPlatformVar":
                return new GetPlatformVar(
                        (String) anyMsgJsonObject.get("sender"),
                        (String) anyMsgJsonObject.get("name"),
                        ((String) anyMsgJsonObject.get("clientAddress")).getBytes()
                );
            case "org.mixql.remote.messages.module.worker.GetPlatformVars":
                return new GetPlatformVars(
                        (String) anyMsgJsonObject.get("sender"),
                        parseStringsArray((JSONArray) anyMsgJsonObject.get("names")),
                        ((String) anyMsgJsonObject.get("clientAddress")).getBytes()
                );
            case "org.mixql.remote.messages.module.worker.GetPlatformVarsNames":
                return new GetPlatformVarsNames(
                        (String) anyMsgJsonObject.get("sender"),
                        ((String) anyMsgJsonObject.get("clientAddress")).getBytes()
                );
            case "org.mixql.remote.messages.client.PlatformVar":
                return new PlatformVar(
                        (String) anyMsgJsonObject.get("sender"),
                        (String) anyMsgJsonObject.get("name"),
                        _unpackAnyMsg((JSONObject) anyMsgJsonObject.get("msg"))
                );
            case "org.mixql.remote.messages.client.PlatformVars":
                Message[] messageArray = parseMessagesArray((JSONArray) anyMsgJsonObject
                        .get("vars")
                );

                Param[] paramsArray = Arrays.copyOf(messageArray, messageArray.length, Param[].class);
                return new PlatformVars(
                        (String) anyMsgJsonObject.get("sender"),
                        paramsArray
                );
            case "org.mixql.remote.messages.client.PlatformVarsNames":
                return new PlatformVarsNames(
                        (String) anyMsgJsonObject.get("sender"),
                        parseStringsArray((JSONArray) anyMsgJsonObject.get("names"))
                );
            case "org.mixql.remote.messages.client.PlatformVarsWereSet":
                return new PlatformVarsWereSet(
                        (String) anyMsgJsonObject.get("sender"),
                        new ArrayList<String>(
                                Arrays.asList(parseStringsArray((JSONArray) anyMsgJsonObject.get("names")))
                        )
                );
            case "org.mixql.remote.messages.client.PlatformVarWasSet":
                return new PlatformVarWasSet(
                        (String) anyMsgJsonObject.get("sender"),
                        (String) anyMsgJsonObject.get("name")
                );
            case "org.mixql.remote.messages.module.worker.SendMsgToPlatform":
                return new SendMsgToPlatform(
                        ((String) anyMsgJsonObject.get("clientAddress")).getBytes(),
                        _unpackAnyMsg((JSONObject) anyMsgJsonObject.get("msg")),
                        (String) anyMsgJsonObject.get("sender")
                );
            case "org.mixql.remote.messages.module.worker.SetPlatformVar":
                return new SetPlatformVar(
                        (String) anyMsgJsonObject.get("sender"),
                        (String) anyMsgJsonObject.get("name"),
                        _unpackAnyMsg((JSONObject) anyMsgJsonObject.get("msg")),
                        ((String) anyMsgJsonObject.get("clientAddress")).getBytes()
                );
            case "org.mixql.remote.messages.module.worker.SetPlatformVars":
                JSONArray varsJsonObject = (JSONArray) anyMsgJsonObject.get("vars");
                Map<String, Message> varsMap = new HashMap<>();
                for (int i = 0; i < varsJsonObject.size(); i++) {
                    varsMap.put(
                            (String) ((JSONObject) varsJsonObject.get(i)).get("key"),
                            _unpackAnyMsg(
                                    (JSONObject) ((JSONObject) varsJsonObject.get(i)).get("value")
                            )
                    );
                }
                return new SetPlatformVars(
                        (String) anyMsgJsonObject.get("sender"),
                        varsMap,
                        ((String) anyMsgJsonObject.get("clientAddress")).getBytes()
                );
            case "org.mixql.remote.messages.module.worker.WorkerFinished":
                return new WorkerFinished(
                        (String) anyMsgJsonObject.get("sender")
                );
            case "org.mixql.remote.messages.module.worker.InvokeFunction":
                return new InvokeFunction(
                        (String) anyMsgJsonObject.get("sender"),
                        (String) anyMsgJsonObject.get("name"),
                        parseMessagesArray((JSONArray) anyMsgJsonObject
                                .get("args")
                        ),
                        ((String) anyMsgJsonObject.get("clientAddress")).getBytes()
                );
            case "org.mixql.remote.messages.client.InvokedFunctionResult":
                return new InvokedPlatformFunctionResult(
                        (String) anyMsgJsonObject.get("sender"),
                        (String) anyMsgJsonObject.get("name"),
                        _unpackAnyMsg((JSONObject) anyMsgJsonObject.get("result"))
                );
            case "org.mixql.remote.messages.cluster.EngineStarted":
                return new EngineStarted(
                        (String) anyMsgJsonObject.get("engineName")
                );
        }
        throw new Exception("_unpackAnyMsg: unknown anyMsgJsonObject" + anyMsgJsonObject);
    }

    public static Message unpackAnyMsg(String json) {

        try {
            JSONObject anyMsgJsonObject = (JSONObject) JSONValue.parseWithException(json);
            return _unpackAnyMsg(anyMsgJsonObject);
        } catch (Exception e) {
            return new Error(
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
        if (msg instanceof ShutDown) {
            return JsonUtils.buildShutDown(msg.type());
        }

        if (msg instanceof Execute) {
            return JsonUtils.buildExecute(msg.type(), ((Execute) msg).statement, ((Execute) msg).moduleIdentity);
        }

        if (msg instanceof Param) {
            return JsonUtils.buildParam(msg.type(), ((Param) msg).name, _toJsonObject(((Param) msg).msg));
        }

        if (msg instanceof Error) {
            return JsonUtils.buildError(msg.type(), ((Error) msg).msg);
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

        if (msg instanceof NONE) {
            return JsonUtils.buildNONE(msg.type());
        }

        if (msg instanceof Bool) {
            return JsonUtils.buildBool(msg.type(), ((Bool) msg).value);
        }

        if (msg instanceof gInt) {
            return JsonUtils.buildInt(msg.type(), ((gInt) msg).value);
        }

        if (msg instanceof gDouble) {
            return JsonUtils.buildDouble(msg.type(), ((gDouble) msg).value);
        }

        if (msg instanceof gString) {
            return JsonUtils.buildGString(msg.type(), ((gString) msg).value, ((gString) msg).quote);
        }

        if (msg instanceof gArray) {
            return JsonUtils.buildGArray(msg.type(), _toJsonObjects(((gArray) msg).arr));
        }

        if (msg instanceof map) {
            Set<Message> keys = ((map) msg).getMap().keySet();
            Collection<Message> values = ((map) msg).getMap().values();
            return JsonUtils.buildMap(msg.type(), _toJsonObjects(keys.toArray(new Message[keys.size()])),
                    _toJsonObjects(values.toArray(new Message[values.size()])));
        }

        if (msg instanceof GetPlatformVar) {
            GetPlatformVar msgVar = ((GetPlatformVar) msg);
            return JsonUtils.buildGetPlatformVar(msg.type(), msgVar.name, msgVar.workerIdentity(),
                    new String(msgVar.clientIdentity()));
        }

        if (msg instanceof GetPlatformVars) {
            GetPlatformVars msgVars = ((GetPlatformVars) msg);
            return JsonUtils.buildGetPlatformVars(msg.type(), msgVars.names, msgVars.workerIdentity(),
                    new String(msgVars.clientIdentity()));
        }

        if (msg instanceof GetPlatformVarsNames) {
            GetPlatformVarsNames msgTmp = ((GetPlatformVarsNames) msg);
            return JsonUtils.buildGetPlatformVarsNames(msgTmp.type(), msgTmp.workerIdentity(),
                    new String(msgTmp.clientIdentity()));
        }

        if (msg instanceof PlatformVar) {
            PlatformVar msgTmp = ((PlatformVar) msg);
            return JsonUtils.buildPlatformVar(msgTmp.type(), msgTmp.workerIdentity(),
                    msgTmp.name, _toJsonObject(msgTmp.msg));
        }

        if (msg instanceof PlatformVars) {
            PlatformVars msgTmp = ((PlatformVars) msg);
            return JsonUtils.buildPlatformVars(msgTmp.type(), msgTmp.workerIdentity(),
                    _toJsonObjects(msgTmp.vars));
        }

        if (msg instanceof PlatformVarsNames) {
            PlatformVarsNames msgTmp = ((PlatformVarsNames) msg);
            return JsonUtils.buildPlatformVarsNames(msgTmp.type(), msgTmp.names, msgTmp.workerIdentity());
        }

        if (msg instanceof PlatformVarsWereSet) {
            PlatformVarsWereSet msgTmp = ((PlatformVarsWereSet) msg);
            return JsonUtils.buildPlatformVarsWereSet(msgTmp.type(), msgTmp.names.toArray(new String[0]),
                    msgTmp.workerIdentity());
        }

        if (msg instanceof PlatformVarWasSet) {
            PlatformVarWasSet msgTmp = ((PlatformVarWasSet) msg);
            return JsonUtils.buildPlatformVarWasSet(msgTmp.type(), msgTmp.name,
                    msgTmp.workerIdentity());
        }

        if (msg instanceof SendMsgToPlatform) {
            SendMsgToPlatform msgTmp = ((SendMsgToPlatform) msg);
            return JsonUtils.buildSendMsgToPlatform(msgTmp.type(),
                    msgTmp.workerIdentity(),
                    new String(msgTmp.clientIdentity()),
                    _toJsonObject(msgTmp.msg)
            );
        }

        if (msg instanceof SetPlatformVar) {
            SetPlatformVar msgTmp = ((SetPlatformVar) msg);
            return JsonUtils.buildSetPlatformVar(msgTmp.type(),
                    msgTmp.workerIdentity(),
                    msgTmp.name,
                    _toJsonObject(msgTmp.msg),
                    new String(msgTmp.clientIdentity())
            );
        }

        if (msg instanceof WorkerFinished) {
            WorkerFinished msgTmp = ((WorkerFinished) msg);
            return JsonUtils.buildWorkerFinished(msgTmp.type(),
                    msgTmp.workerIdentity()
            );
        }

        if (msg instanceof SetPlatformVars) {
            SetPlatformVars msgTmp = ((SetPlatformVars) msg);

            return JsonUtils.buildSetPlatformVars(msgTmp.type(),
                    msgTmp.workerIdentity(),
                    new String(msgTmp.clientIdentity()),
                    msgTmp.vars.keySet().toArray(new String[msgTmp.vars.keySet().size()]),
                    _toJsonObjects(
                            msgTmp.vars.values().toArray(
                                    new Message[msgTmp.vars.values().size()]
                            )
                    )
            );
        }

        if (msg instanceof InvokeFunction) {
            InvokeFunction msgTmp = ((InvokeFunction) msg);
            return JsonUtils.buildInvokeFunction(msgTmp.type(),
                    msgTmp.workerIdentity(), msgTmp.name, _toJsonObjects(msgTmp.args), new String(msgTmp.clientIdentity())
            );
        }

        if (msg instanceof InvokedPlatformFunctionResult) {
            InvokedPlatformFunctionResult msgTmp = ((InvokedPlatformFunctionResult) msg);
            return JsonUtils.buildInvokedFunctionResult(msgTmp.type(),
                    msgTmp.workerIdentity(), msgTmp.name, _toJsonObject(msgTmp.result)
            );
        }

        if (msg instanceof EngineStarted) {
            EngineStarted msgTmp = ((EngineStarted) msg);
            return JsonUtils.buildEngineStarted(msgTmp.type(), msgTmp.engineName);
        }

        throw new Exception("_toJsonObject Error. Unknown type of message " + msg);
    }

    public static String toJson(Message msg) throws Exception {
        return _toJsonObject(msg).toJSONString();
    }
}
