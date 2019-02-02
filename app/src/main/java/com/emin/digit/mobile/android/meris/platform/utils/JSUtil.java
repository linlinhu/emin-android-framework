package com.emin.digit.mobile.android.meris.platform.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * author: Samson
 * created on: 2017/1/21
 * description:
 * JS 工具
 */
public class JSUtil {

    public static final String TAG = JSUtil.class.getSimpleName();

    public static String wrapKeyValue(String key, Object value){
        //String result = "{" + key + ":'%s'}";
        //result = String.format(result,value);

        JSONObject json = new JSONObject();
        try {
            json.put(key, value);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return json.toString();
    }

    public static JSONObject jsonObjectFromString(String str) {
        try {
            JSONObject obj = new JSONObject(str);
            return obj;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Exception occurred:" + e.getMessage());
        }
        return null;
    }
}
