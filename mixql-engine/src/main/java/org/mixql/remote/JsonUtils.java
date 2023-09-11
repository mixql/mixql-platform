package org.mixql.remote;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;

class JsonUtils {
    public static JSONObject buildEngineStarted(String type, String name) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("engineName", name);
        return jsonObject;
    }

    public static JSONObject buildEngineIsReady(String type, String name) {
        return buildEngineStarted(type, name);
    }

    public static JSONObject buildEnginePingHeartBeat(String type, String name) {
        return buildEngineStarted(type, name);
    }

    public static JSONObject buildShutDown(String type, String moduleIdentity, String clientIdentity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("moduleIdentity", moduleIdentity);
        jsonObject.put("clientIdentity", clientIdentity);
        return jsonObject;
    }

    public static JSONObject buildGetDefinedFunctions(String type, String moduleIdentity, String clientIdentity) {
        return buildShutDown(type, moduleIdentity, clientIdentity);
    }

    public static JSONObject buildNULL(String type) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        return jsonObject;
    }

    public static JSONObject buildNONE(String type) {
        return buildNULL(type);
    }

    public static JSONObject buildPlatformPongHeartBeat(String type) {
        return buildNULL(type);
    }

    public static JSONObject buildExecute(String type, String statement, String moduleIdentity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("statement", statement);
        jsonObject.put("moduleIdentity", moduleIdentity);
        return jsonObject;
    }

    public static JSONObject buildParam(String type, String name, JSONObject msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("name", name);
        jsonObject.put("msg", msg);
        return jsonObject;
    }

    public static JSONObject buildExecutedFunctionResult(String type, String functionName, JSONObject msg,
                                                         String clientIdentity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("functionName", functionName);
        jsonObject.put("msg", msg);
        jsonObject.put("clientIdentity", clientIdentity);
        return jsonObject;
    }

    public static JSONObject buildExecuteResult(String type, String stmt, JSONObject result,
                                                         String clientIdentity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("stmt", stmt);
        jsonObject.put("result", result);
        jsonObject.put("clientIdentity", clientIdentity);
        return jsonObject;
    }

    public static JSONObject buildError(String type, String msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("errorMsg", msg);
        return jsonObject;
    }

    public static JSONObject buildEngineFailed(String type, String engineName, String msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("engineName", engineName);
        jsonObject.put("errorMsg", msg);
        return jsonObject;
    }

    public static JSONObject buildGetDefinedFunctionsError(String type, String clientIdentity, String msg) {
        return buildExecutedFunctionResultFailed(type, clientIdentity, msg);
    }

    public static JSONObject buildExecuteResultFailed(String type, String clientIdentity, String msg) {
        return buildExecutedFunctionResultFailed(type, clientIdentity, msg);
    }

    public static JSONObject buildExecutedFunctionResultFailed(String type, String clientIdentity, String msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("clientIdentity", clientIdentity);
        jsonObject.put("errorMsg", msg);
        return jsonObject;
    }

    public static JSONObject buildExecuteFunction(String type, String moduleIdentity, String clientIdentity,
                                                  String name, JSONObject[] params) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("moduleIdentity", moduleIdentity);
        jsonObject.put("clientIdentity", clientIdentity);
        jsonObject.put("name", name);
        jsonObject.put("params", buildJsonObjectsArray(params));
        return jsonObject;
    }

    private static JSONArray buildStringArray(String[] arr) {
        JSONArray jsonArrObject = new JSONArray();
        jsonArrObject.addAll(Arrays.asList(arr));
        return jsonArrObject;
    }

    private static JSONArray buildJsonObjectsArray(JSONObject[] arr) {
        JSONArray jsonArrObject = new JSONArray();
        jsonArrObject.addAll(Arrays.asList(arr));
        return jsonArrObject;
    }

    public static JSONObject buildDefinedFunction(String type, String[] arr, String clientIdentity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("arr", buildStringArray(arr));
        jsonObject.put("clientIdentity", clientIdentity);
        return jsonObject;
    }

    public static JSONObject buildBool(String type, Boolean value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("value", value.toString());
        return jsonObject;
    }

    public static JSONObject buildInt(String type, Integer value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("value", value.toString());
        return jsonObject;
    }

    public static JSONObject buildDouble(String type, Double value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("value", value.toString());
        return jsonObject;
    }

    public static JSONObject buildGString(String type, String value, String quote) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("value", value);
        jsonObject.put("quote", quote);
        return jsonObject;
    }

    public static JSONObject buildGArray(String type, JSONObject[] arr) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("arr", buildJsonObjectsArray(arr));
        return jsonObject;
    }

    public static JSONObject buildMap(String type, JSONObject[] keys, JSONObject[] values) {
        JSONObject mapJsonObject = new JSONObject();
        mapJsonObject.put("type", type);

        JSONArray jsonArrObject = new JSONArray();
        for (int i = 0; i < keys.length; i++) {
            JSONObject tupleJsonObject = new JSONObject();
            tupleJsonObject.put("key", keys[i]);
            tupleJsonObject.put("value", values[i]);
            jsonArrObject.add(tupleJsonObject);
        }
        mapJsonObject.put("map", jsonArrObject);
        return mapJsonObject;
    }

    public static JSONObject buildGetPlatformVar(String type, String varName, String workerID, String clientIdentity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("name", varName);
        jsonObject.put("worker", workerID);
        jsonObject.put("clientIdentity", clientIdentity);
        return jsonObject;
    }

    public static JSONObject buildGetPlatformVarsNames(String type, String workerID, String clientIdentity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("worker", workerID);
        jsonObject.put("clientIdentity", clientIdentity);
        return jsonObject;
    }

    public static JSONObject buildGetPlatformVars(String type, String[] varNames, String workerID, String clientIdentity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);

        jsonObject.put("names", buildStringArray(varNames));
        jsonObject.put("worker", workerID);
        jsonObject.put("clientIdentity", clientIdentity);
        return jsonObject;
    }

    public static JSONObject buildPlatformVar(String type, String moduleIdentity, String clientIdentity,
                                              String workerID, String name,
                                              JSONObject msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("moduleIdentity", moduleIdentity);
        jsonObject.put("clientIdentity", clientIdentity);
        jsonObject.put("worker", workerID);
        jsonObject.put("msg", msg);
        jsonObject.put("name", name);
        return jsonObject;
    }

    public static JSONObject buildInvokedFunctionResult(String type, String moduleIdentity, String clientIdentity,
                                                        String workerID, String name,
                                                        JSONObject result) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("moduleIdentity", moduleIdentity);
        jsonObject.put("clientIdentity", clientIdentity);
        jsonObject.put("worker", workerID);
        jsonObject.put("result", result);
        jsonObject.put("name", name);
        return jsonObject;
    }

    public static JSONObject buildPlatformVars(String type, String moduleIdentity, String clientIdentity,
                                               String workerID, JSONObject[] vars) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("moduleIdentity", moduleIdentity);
        jsonObject.put("clientIdentity", clientIdentity);
        jsonObject.put("worker", workerID);
        jsonObject.put("vars", buildJsonObjectsArray(vars));
        return jsonObject;
    }

    public static JSONObject buildInvokeFunction(String type, String workerID, String name, JSONObject[] args,
                                                 String clientIdentity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("worker", workerID);
        jsonObject.put("name", name);
        jsonObject.put("args", buildJsonObjectsArray(args));
        jsonObject.put("clientIdentity", clientIdentity);
        return jsonObject;
    }

    public static JSONObject buildPlatformVarsNames(String type, String moduleIdentity, String clientIdentity,
                                                    String[] names, String workerID) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("moduleIdentity", moduleIdentity);
        jsonObject.put("clientIdentity", clientIdentity);
        jsonObject.put("names", buildStringArray(names));
        jsonObject.put("worker", workerID);
        return jsonObject;
    }

    public static JSONObject buildPlatformVarsWereSet(String type, String moduleIdentity, String clientIdentity,
                                                      String[] names, String workerID) {
        return buildPlatformVarsNames(type, moduleIdentity, clientIdentity, names, workerID);
    }

    public static JSONObject buildPlatformVarWasSet(String type, String moduleIdentity, String clientIdentity,
                                                    String name, String workerID) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("moduleIdentity", moduleIdentity);
        jsonObject.put("clientIdentity", clientIdentity);
        jsonObject.put("worker", workerID);
        jsonObject.put("name", name);
        return jsonObject;
    }

    public static JSONObject buildSendMsgToPlatform(String type, JSONObject msg, String workerID) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("worker", workerID);
        jsonObject.put("msg", msg);
        return jsonObject;
    }

    public static JSONObject buildSetPlatformVar(String type, String workerID, String name,
                                                 JSONObject msg, String clientIdentity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("worker", workerID);
        jsonObject.put("msg", msg);
        jsonObject.put("name", name);
        jsonObject.put("clientIdentity", clientIdentity);
        return jsonObject;
    }

    public static JSONObject buildSetPlatformVars(String type, String workerID, String clientIdentity,
                                                  String[] keys, JSONObject[] values) {
        JSONObject mapJsonObject = new JSONObject();
        mapJsonObject.put("type", type);
        mapJsonObject.put("worker", workerID);
        mapJsonObject.put("clientIdentity", clientIdentity);

        JSONArray jsonArrObject = new JSONArray();
        for (int i = 0; i < keys.length; i++) {
            JSONObject tupleJsonObject = new JSONObject();
            tupleJsonObject.put("key", keys[i]);
            tupleJsonObject.put("value", values[i]);
            jsonArrObject.add(tupleJsonObject);
        }
        mapJsonObject.put("vars", jsonArrObject);
        return mapJsonObject;
    }

    public static JSONObject buildWorkerFinished(String type, String workerID) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("worker", workerID);
        return jsonObject;
    }

}
