package com.emin.digit.mobile.android.meris.platform.components.location.converter;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * author: Samson
 * created on: 2017/11/10 0010 下午 3:22
 * description:
 * 位置信息转换器
 * 如:转成JSON格式、DTO等应用层所需要的特定格式的数据
 */
public class LocationConverter {

    public static JSONObject convertToJSON(Location location) {
        JSONObject json = new JSONObject();
        try {
            json.put("provider", location.getProvider());
            json.put("latitude", location.getLatitude());
            json.put("longitude", location.getLongitude());
            json.put("accuracy", location.getAccuracy());
            json.put("time", location.getTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
