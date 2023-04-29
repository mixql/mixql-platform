package org.mixql.protobuf.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;

public class JsonUtils {
    public static String buildEngineName(String name) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        return jsonObject.toJSONString();
    }

    public static String buildAnyMsg(String type, String json) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("json", json);
        return jsonObject.toJSONString();
    }

    public static String buildGetParam(String name) {
        return buildEngineName(name);
    }

    public static String buildIsParam(String name) {
        return buildEngineName(name);
    }
    public static String buildExecute(String statement) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("statement", statement);
        return jsonObject.toJSONString();
    }

    public static String buildParam(String name, String json) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        jsonObject.put("json", json);
        return jsonObject.toJSONString();
    }

    public static String buildSetParam(String name, String json) {
        return buildParam(name, json);
    }

    public static String buildError(String msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", msg);
        return jsonObject.toJSONString();
    }

    public static String buildExecuteFunction(String name, String[] params) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        jsonObject.put("params", _buildGArray(params));
        return jsonObject.toJSONString();
    }

    private static JSONArray buildStringArray(String[] arr) {
        JSONArray jsonArrObject = new JSONArray();
        jsonArrObject.addAll(Arrays.stream(arr).toList());
        return jsonArrObject;
    }

    public static String buildDefinedFunction(String[] arr) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("arr", buildStringArray(arr));
        return jsonObject.toJSONString();
    }

    public static String buildBool(Boolean value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", value.toString());
        return jsonObject.toJSONString();
    }

    public static String buildInt(Integer value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", value.toString());
        return jsonObject.toJSONString();
    }

    public static String buildDouble(Double value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", value.toString());
        return jsonObject.toJSONString();
    }

    public static String buildGString(String value, String quote) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", value);
        jsonObject.put("quote", quote);
        return jsonObject.toJSONString();
    }

    public static String buildGArray(String[] arr) {
        return _buildGArray(arr).toJSONString();
    }

    private static JSONObject _buildGArray(String[] arr) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("arr", buildStringArray(arr));
        return jsonObject;
    }


}
