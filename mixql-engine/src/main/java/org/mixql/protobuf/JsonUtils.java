package org.mixql.protobuf;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mixql.protobuf.messages.Message;

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

    public static JSONObject buildParamWasSet(String type) {
        return buildShutDown(type);
    }

    public static JSONObject buildGetDefinedFunctions(String type) {
        return buildShutDown(type);
    }

    public static JSONObject buildNULL(String type) {
        return buildShutDown(type);
    }

    public static JSONObject buildGetParam(String type, String name) {
        return buildEngineName(type, name);
    }

    public static JSONObject buildIsParam(String type, String name) {
        return buildEngineName(type, name);
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

    public static JSONObject buildSetParam(String type, String name, JSONObject msg) {
        return buildParam(type, name, msg);
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
        jsonObject.put("params", buildJsobObjectsArray(params));
        return jsonObject;
    }

    private static JSONArray buildStringArray(String[] arr) {
        JSONArray jsonArrObject = new JSONArray();
        jsonArrObject.addAll(Arrays.asList(arr));
        return jsonArrObject;
    }

    private static JSONArray buildJsobObjectsArray(JSONObject[] arr) {
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
        jsonObject.put("arr", buildJsobObjectsArray(arr));
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

}
