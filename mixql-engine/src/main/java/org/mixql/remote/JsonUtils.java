package org.mixql.remote;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;

class JsonUtils {
    public static JSONObject buildEngineName(String type, String name) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("name", name);
        return jsonObject;
    }

    public static JSONObject buildShutDown(String type) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        return jsonObject;
    }

    public static JSONObject buildGetDefinedFunctions(String type) {
        return buildShutDown(type);
    }

    public static JSONObject buildNULL(String type) {
        return buildShutDown(type);
    }

    public static JSONObject buildExecute(String type, String statement) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("statement", statement);
        return jsonObject;
    }

    public static JSONObject buildParam(String type, String name, JSONObject msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("name", name);
        jsonObject.put("msg", msg);
        return jsonObject;
    }

    public static JSONObject buildError(String type, String msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("msg", msg);
        return jsonObject;
    }

    public static JSONObject buildExecuteFunction(String type, String name, JSONObject[] params) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
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

    public static JSONObject buildDefinedFunction(String type, String[] arr) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("arr", buildStringArray(arr));
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

    public static JSONObject buildGetPlatformVar(String type, String varName, String senderID, String clientAddress) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("name", varName);
        jsonObject.put("sender", senderID);
        jsonObject.put("clientAddress", clientAddress);
        return jsonObject;
    }

    public static JSONObject buildGetPlatformVarsNames(String type, String senderID, String clientAddress) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("sender", senderID);
        jsonObject.put("clientAddress", clientAddress);
        return jsonObject;
    }

    public static JSONObject buildGetPlatformVars(String type, String[] varNames, String senderID, String clientAddress) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);

        jsonObject.put("names", buildStringArray(varNames));
        jsonObject.put("sender", senderID);
        jsonObject.put("clientAddress", clientAddress);
        return jsonObject;
    }

    public static JSONObject buildPlatformVar(String type, String senderID, String name,
                                              JSONObject msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("sender", senderID);
        jsonObject.put("msg", msg);
        jsonObject.put("name", name);
        return jsonObject;
    }

    public static JSONObject buildPlatformVars(String type, String senderID, JSONObject[] vars) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("sender", senderID);
        jsonObject.put("vars", buildJsonObjectsArray(vars));
        return jsonObject;
    }

    public static JSONObject buildPlatformVarsNames(String type, String[] names, String senderID) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);

        jsonObject.put("names", buildStringArray(names));
        jsonObject.put("sender", senderID);
        return jsonObject;
    }

    public static JSONObject buildPlatformVarsWereSet(String type, String[] names, String senderID) {
        return buildPlatformVarsNames(type, names, senderID);
    }

    public static JSONObject buildPlatformVarWasSet(String type, String name, String senderID) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("sender", senderID);
        jsonObject.put("name", name);
        return jsonObject;
    }

    public static JSONObject buildSendMsgToPlatform(String type, String senderID, String clientAddress,
                                                    JSONObject msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("sender", senderID);
        jsonObject.put("clientAddress", clientAddress);
        jsonObject.put("msg", msg);
        return jsonObject;
    }

    public static JSONObject buildSetPlatformVar(String type, String senderID, String name,
                                                 JSONObject msg) {
        return buildPlatformVar(type, senderID, name, msg);
    }

    public static JSONObject buildSetPlatformVars(String type, String senderID, String clientAddress,
                                                  String[] keys, JSONObject[] values) {
        JSONObject mapJsonObject = new JSONObject();
        mapJsonObject.put("type", type);
        mapJsonObject.put("sender", senderID);
        mapJsonObject.put("clientAddress", clientAddress);

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

    public static JSONObject buildWorkerFinished(String type, String senderID) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("sender", senderID);
        return jsonObject;
    }

}
