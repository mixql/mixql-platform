package org.mixql.remote;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mixql.remote.messages.*;
import org.mixql.remote.messages.client.*;
import org.mixql.remote.messages.client.toBroker.EngineStarted;
import org.mixql.remote.messages.module.*;
import org.mixql.remote.messages.module.fromBroker.PlatformPongHeartBeat;
import org.mixql.remote.messages.module.toBroker.EngineFailed;
import org.mixql.remote.messages.module.toBroker.EngineIsReady;
import org.mixql.remote.messages.module.toBroker.EnginePingHeartBeat;
import org.mixql.remote.messages.type.gtype.*;
import org.mixql.remote.messages.type.Param;
import org.mixql.remote.messages.module.worker.*;
import org.mixql.remote.messages.type.Error;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.PrintWriter;
import java.io.StringWriter;


public class RemoteMessageConverter {
    public static Message unpackAnyMsgFromArray(byte[] array) throws IOException {
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
                return new ShutDown(
                        (String) anyMsgJsonObject.get("moduleIdentity"),
                        (String) anyMsgJsonObject.get("clientIdentity")
                );
            case "org.mixql.remote.messages.client.Execute":
                return new Execute(
                        (String) anyMsgJsonObject.get("moduleIdentity"),
                        (String) anyMsgJsonObject.get("clientIdentity"),
                        (String) anyMsgJsonObject.get("statement")
                );
            case "org.mixql.remote.messages.type.Param":
                return new Param(
                        (String) anyMsgJsonObject.get("name"),
                        _unpackAnyMsg((JSONObject) anyMsgJsonObject.get("msg"))
                );
            case "org.mixql.remote.messages.type.Error":
                return new Error(
                        "error while unpacking from json Error: " + anyMsgJsonObject.get("errorMsg")
                );
            case "org.mixql.remote.messages.client.ExecuteFunction":
                return new ExecuteFunction(
                        (String) anyMsgJsonObject.get("moduleIdentity"),
                        (String) anyMsgJsonObject.get("clientIdentity"),
                        (String) anyMsgJsonObject.get("name"),
                        parseMessagesArray((JSONArray) anyMsgJsonObject
                                .get("params")
                        )
                );
            case "org.mixql.remote.messages.client.GetDefinedFunctions":
                return new GetDefinedFunctions(
                        (String) anyMsgJsonObject.get("moduleIdentity"),
                        (String) anyMsgJsonObject.get("clientIdentity")
                );
            case "org.mixql.remote.messages.module.DefinedFunctions":
                return new DefinedFunctions(
                        parseStringsArray((JSONArray) anyMsgJsonObject.get("arr")),
                        (String) anyMsgJsonObject.get("clientIdentity")
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
                        (String) anyMsgJsonObject.get("worker"),
                        (String) anyMsgJsonObject.get("name"),
                        (String) anyMsgJsonObject.get("clientIdentity")
                );
            case "org.mixql.remote.messages.module.worker.GetPlatformVars":
                return new GetPlatformVars(
                        (String) anyMsgJsonObject.get("worker"),
                        parseStringsArray((JSONArray) anyMsgJsonObject.get("names")),
                        (String) anyMsgJsonObject.get("clientIdentity")
                );
            case "org.mixql.remote.messages.module.worker.GetPlatformVarsNames":
                return new GetPlatformVarsNames(
                        (String) anyMsgJsonObject.get("worker"),
                        (String) anyMsgJsonObject.get("clientIdentity")
                );
            case "org.mixql.remote.messages.client.PlatformVar":
                return new PlatformVar(
                        (String) anyMsgJsonObject.get("moduleIdentity"),
                        (String) anyMsgJsonObject.get("clientIdentity"),
                        (String) anyMsgJsonObject.get("worker"),
                        (String) anyMsgJsonObject.get("name"),
                        _unpackAnyMsg((JSONObject) anyMsgJsonObject.get("msg"))
                );
            case "org.mixql.remote.messages.client.PlatformVars":
                Message[] messageArray = parseMessagesArray((JSONArray) anyMsgJsonObject
                        .get("vars")
                );

                Param[] paramsArray = Arrays.copyOf(messageArray, messageArray.length, Param[].class);
                return new PlatformVars(
                        (String) anyMsgJsonObject.get("moduleIdentity"),
                        (String) anyMsgJsonObject.get("clientIdentity"),
                        (String) anyMsgJsonObject.get("worker"),
                        paramsArray
                );
            case "org.mixql.remote.messages.client.PlatformVarsNames":
                return new PlatformVarsNames(
                        (String) anyMsgJsonObject.get("moduleIdentity"),
                        (String) anyMsgJsonObject.get("clientIdentity"),
                        (String) anyMsgJsonObject.get("worker"),
                        parseStringsArray((JSONArray) anyMsgJsonObject.get("names"))
                );
            case "org.mixql.remote.messages.client.PlatformVarsWereSet":
                return new PlatformVarsWereSet(
                        (String) anyMsgJsonObject.get("moduleIdentity"),
                        (String) anyMsgJsonObject.get("clientIdentity"),
                        (String) anyMsgJsonObject.get("worker"),
                        new ArrayList<String>(
                                Arrays.asList(parseStringsArray((JSONArray) anyMsgJsonObject.get("names")))
                        )
                );
            case "org.mixql.remote.messages.client.PlatformVarWasSet":
                return new PlatformVarWasSet(
                        (String) anyMsgJsonObject.get("moduleIdentity"),
                        (String) anyMsgJsonObject.get("clientIdentity"),
                        (String) anyMsgJsonObject.get("worker"),
                        (String) anyMsgJsonObject.get("name")
                );
            case "org.mixql.remote.messages.module.worker.SendMsgToPlatform":
                return new SendMsgToPlatform(
                        (IModuleSendToClient) _unpackAnyMsg((JSONObject) anyMsgJsonObject.get("msg")),
                        (String) anyMsgJsonObject.get("worker")
                );
            case "org.mixql.remote.messages.module.worker.SetPlatformVar":
                return new SetPlatformVar(
                        (String) anyMsgJsonObject.get("worker"),
                        (String) anyMsgJsonObject.get("name"),
                        _unpackAnyMsg((JSONObject) anyMsgJsonObject.get("msg")),
                        (String) anyMsgJsonObject.get("clientIdentity")
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
                        (String) anyMsgJsonObject.get("worker"),
                        varsMap,
                        (String) anyMsgJsonObject.get("clientIdentity")
                );
            case "org.mixql.remote.messages.module.worker.WorkerFinished":
                return new WorkerFinished(
                        (String) anyMsgJsonObject.get("worker")
                );
            case "org.mixql.remote.messages.module.worker.InvokeFunction":
                return new InvokeFunction(
                        (String) anyMsgJsonObject.get("worker"),
                        (String) anyMsgJsonObject.get("name"),
                        parseMessagesArray((JSONArray) anyMsgJsonObject
                                .get("args")
                        ),
                        (String) anyMsgJsonObject.get("clientIdentity")
                );
            case "org.mixql.remote.messages.client.InvokedPlatformFunctionResult":
                return new InvokedPlatformFunctionResult(
                        (String) anyMsgJsonObject.get("moduleIdentity"),
                        (String) anyMsgJsonObject.get("clientIdentity"),
                        (String) anyMsgJsonObject.get("worker"),
                        (String) anyMsgJsonObject.get("name"),
                        _unpackAnyMsg((JSONObject) anyMsgJsonObject.get("result"))
                );
            case "org.mixql.remote.messages.client.toBroker.EngineStarted":
                return new EngineStarted(
                        (String) anyMsgJsonObject.get("engineName"),
                        (String) anyMsgJsonObject.get("clientIdentity")
                );
            case "org.mixql.remote.messages.module.fromBroker.PlatformPongHeartBeat":
                return new PlatformPongHeartBeat();
            case "org.mixql.remote.messages.module.toBroker.EngineFailed":
                return new EngineFailed(
                        (String) anyMsgJsonObject.get("engineName"),
                        (String) anyMsgJsonObject.get("errorMsg")
                );
            case "org.mixql.remote.messages.module.toBroker.EngineIsReady":
                return new EngineIsReady(
                        (String) anyMsgJsonObject.get("engineName")
                );
            case "org.mixql.remote.messages.module.toBroker.EnginePingHeartBeat":
                return new EnginePingHeartBeat(
                        (String) anyMsgJsonObject.get("engineName")
                );
            case "org.mixql.remote.messages.module.ExecutedFunctionResult":
                return new ExecutedFunctionResult(
                        (String) anyMsgJsonObject.get("functionName"),
                        _unpackAnyMsg((JSONObject) anyMsgJsonObject.get("msg")),
                        (String) anyMsgJsonObject.get("clientIdentity")
                );
            case "org.mixql.remote.messages.module.ExecutedFunctionResultFailed":
                return new ExecutedFunctionResultFailed(
                        (String) anyMsgJsonObject.get("errorMsg"),
                        (String) anyMsgJsonObject.get("clientIdentity")
                );
            case "org.mixql.remote.messages.module.ExecuteResult":
                return new ExecuteResult(
                        (String) anyMsgJsonObject.get("stmt"),
                        _unpackAnyMsg((JSONObject) anyMsgJsonObject.get("result")),
                        (String) anyMsgJsonObject.get("clientIdentity")
                );
            case "org.mixql.remote.messages.module.ExecuteResultFailed":
                return new ExecuteResultFailed(
                        (String) anyMsgJsonObject.get("errorMsg"),
                        (String) anyMsgJsonObject.get("clientIdentity")
                );
            case "org.mixql.remote.messages.module.GetDefinedFunctionsError":
                return new GetDefinedFunctionsError(
                        (String) anyMsgJsonObject.get("errorMsg"),
                        (String) anyMsgJsonObject.get("clientIdentity")
                );
        }
        throw new Exception("_unpackAnyMsg: unknown anyMsgJsonObject" + anyMsgJsonObject);
    }

    public static Message unpackAnyMsg(String json) throws IOException {

        try {
            JSONObject anyMsgJsonObject = (JSONObject) JSONValue.parseWithException(json);
            return _unpackAnyMsg(anyMsgJsonObject);
        } catch (Exception e) {
            String stackTraceMsg = "target exception stacktrace: ";

            try (
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
            ) {
                e.printStackTrace(pw);
                stackTraceMsg += sw.toString();
            }
            return new Error(
                    String.format(
                            "Protobuf anymsg converter:\n Error: %s \n StackTrace: \n %s", e.getMessage(),
                            stackTraceMsg
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
            ShutDown tmpMsg = (ShutDown) msg;
            return JsonUtils.buildShutDown(msg.type(), tmpMsg.moduleIdentity(), tmpMsg.clientIdentity());
        }

        if (msg instanceof Execute) {
            return JsonUtils.buildExecute(msg.type(), ((Execute) msg).statement, ((Execute) msg).moduleIdentity(),
                    ((Execute) msg).clientIdentity());
        }

        if (msg instanceof Param) {
            return JsonUtils.buildParam(msg.type(), ((Param) msg).name, _toJsonObject(((Param) msg).msg));
        }

        /////////////////////////////////////Error messages//////////////////////////////////////////
        if (msg instanceof EngineFailed) {
            return JsonUtils.buildEngineFailed(msg.type(), ((EngineFailed) msg).engineName(),
                    ((EngineFailed) msg).getErrorMessage());
        }

        if (msg instanceof ExecutedFunctionResultFailed) {
            return JsonUtils.buildExecutedFunctionResultFailed(msg.type(),
                    ((ExecutedFunctionResultFailed) msg).clientIdentity(),
                    ((ExecutedFunctionResultFailed) msg).getErrorMessage());
        }

        if (msg instanceof ExecuteResultFailed) {
            return JsonUtils.buildExecuteResultFailed(msg.type(),
                    ((ExecuteResultFailed) msg).clientIdentity(),
                    ((ExecuteResultFailed) msg).getErrorMessage());
        }

        if (msg instanceof GetDefinedFunctionsError) {
            return JsonUtils.buildGetDefinedFunctionsError(msg.type(),
                    ((GetDefinedFunctionsError) msg).clientIdentity(),
                    ((GetDefinedFunctionsError) msg).getErrorMessage());
        }

        if (msg instanceof Error) {
            return JsonUtils.buildError(msg.type(), ((Error) msg).getErrorMessage());
        }
        /////////////////////////////////////////////////////////////////////////////////////////////

        if (msg instanceof ExecuteFunction) {
            ExecuteFunction tmpMsg = (ExecuteFunction) msg;
            return JsonUtils.buildExecuteFunction(msg.type(), tmpMsg.moduleIdentity, tmpMsg.clientIdentity(),
                    tmpMsg.name,
                    _toJsonObjects(tmpMsg.params)
            );
        }

        if (msg instanceof ExecutedFunctionResult) {
            ExecutedFunctionResult tmpMsg = (ExecutedFunctionResult) msg;
            return JsonUtils.buildExecutedFunctionResult(msg.type(), tmpMsg.functionName, _toJsonObject(tmpMsg.msg),
                    tmpMsg.clientIdentity()
            );
        }

        if (msg instanceof ExecuteResult) {
            ExecuteResult tmpMsg = (ExecuteResult) msg;
            return JsonUtils.buildExecuteResult(msg.type(), tmpMsg.stmt, _toJsonObject(tmpMsg.result),
                    tmpMsg.clientIdentity()
            );
        }

        if (msg instanceof GetDefinedFunctions) {
            GetDefinedFunctions tmpMsg = (GetDefinedFunctions) msg;
            return JsonUtils.buildGetDefinedFunctions(msg.type(), tmpMsg.moduleIdentity(), tmpMsg.clientIdentity());
        }


        if (msg instanceof DefinedFunctions) {
            return JsonUtils.buildDefinedFunction(msg.type(), ((DefinedFunctions) msg).arr,
                    ((DefinedFunctions) msg).clientIdentity());
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
            return JsonUtils.buildPlatformVar(msgTmp.type(), msgTmp.moduleIdentity(),
                    msgTmp.clientIdentity(), msgTmp.workerIdentity(),
                    msgTmp.name, _toJsonObject(msgTmp.msg));
        }

        if (msg instanceof PlatformVars) {
            PlatformVars msgTmp = ((PlatformVars) msg);
            return JsonUtils.buildPlatformVars(msgTmp.type(), msgTmp.moduleIdentity(),
                    msgTmp.clientIdentity(), msgTmp.workerIdentity(),
                    _toJsonObjects(msgTmp.vars));
        }

        if (msg instanceof PlatformVarsNames) {
            PlatformVarsNames msgTmp = ((PlatformVarsNames) msg);
            return JsonUtils.buildPlatformVarsNames(msgTmp.type(), msgTmp.moduleIdentity(),
                    msgTmp.clientIdentity(), msgTmp.names, msgTmp.workerIdentity());
        }

        if (msg instanceof PlatformVarsWereSet) {
            PlatformVarsWereSet msgTmp = ((PlatformVarsWereSet) msg);
            return JsonUtils.buildPlatformVarsWereSet(msgTmp.type(), msgTmp.moduleIdentity(),
                    msgTmp.clientIdentity(), msgTmp.names.toArray(new String[0]),
                    msgTmp.workerIdentity());
        }

        if (msg instanceof PlatformVarWasSet) {
            PlatformVarWasSet msgTmp = ((PlatformVarWasSet) msg);
            return JsonUtils.buildPlatformVarWasSet(msgTmp.type(), msgTmp.moduleIdentity(),
                    msgTmp.clientIdentity(), msgTmp.name,
                    msgTmp.workerIdentity());
        }

        if (msg instanceof SendMsgToPlatform) {
            SendMsgToPlatform msgTmp = ((SendMsgToPlatform) msg);
            return JsonUtils.buildSendMsgToPlatform(msgTmp.type(),
                    _toJsonObject(msgTmp.msg), msgTmp.workerIdentity()
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
            return JsonUtils.buildInvokedFunctionResult(msgTmp.type(), msgTmp.moduleIdentity(),
                    msgTmp.clientIdentity(),
                    msgTmp.workerIdentity(), msgTmp.name, _toJsonObject(msgTmp.result)
            );
        }

        if (msg instanceof EngineStarted) {
            EngineStarted msgTmp = ((EngineStarted) msg);
            return JsonUtils.buildEngineStarted(msgTmp.type(), msgTmp.engineName, msgTmp.clientIdentity());
        }

        if (msg instanceof PlatformPongHeartBeat) {
            PlatformPongHeartBeat msgTmp = ((PlatformPongHeartBeat) msg);
            return JsonUtils.buildPlatformPongHeartBeat(msgTmp.type());
        }

        if (msg instanceof EngineIsReady) {
            EngineIsReady msgTmp = ((EngineIsReady) msg);
            return JsonUtils.buildEngineIsReady(msgTmp.type(), msgTmp.engineName());
        }

        if (msg instanceof EnginePingHeartBeat) {
            EnginePingHeartBeat msgTmp = ((EnginePingHeartBeat) msg);
            return JsonUtils.buildEnginePingHeartBeat(msgTmp.type(), msgTmp.engineName());
        }

        throw new Exception("_toJsonObject Error. Unknown type of message " + msg);
    }

    public static String toJson(Message msg) throws Exception {
        return _toJsonObject(msg).toJSONString();
    }
}
