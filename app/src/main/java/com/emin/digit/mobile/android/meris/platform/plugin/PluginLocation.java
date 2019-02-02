package com.emin.digit.mobile.android.meris.platform.plugin;

import android.location.Location;
import android.util.Log;

import com.emin.digit.mobile.android.meris.platform.components.location.LocationCenter;
import com.emin.digit.mobile.android.meris.platform.components.location.converter.LocationConverter;
import com.emin.digit.mobile.android.meris.platform.core.EMHybridWebView;
import com.emin.digit.mobile.android.meris.platform.core.PluginParams;
import com.emin.digit.mobile.android.meris.platform.utils.WebviewUtil;

/**
 * author: Samson
 * created on: 2017/11/8 下午 3:32
 * description:
 * 定位插件
 */
public class PluginLocation {

    private static final String TAG = PluginLocation.class.getSimpleName();

    /**
     * 获取当前位置信息
     * 如果成功,会持续执行JS成功回调,以位置信息的JSON字符串作为参数;
     * 如果失败,直接执行JS失败回调,以错误信息字符串返回
     *
     * @param params 插件对象,其封装了配置信息(如提供商,精确度等JSON)和JS成功与失败回调方法名
     */
    public void getCurrentPosition(PluginParams params) {
        params.showArguments();
        String okCB = params.getArguments()[0];
        String errorCB = params.getArguments()[1];
        EMHybridWebView wb = (EMHybridWebView)params.getWebView();
        Location location = LocationCenter.getInstance(wb.getActivity()).get();
        String result = LocationConverter.convertToJSON(location).toString();
        WebviewUtil.execCallback(params.getWebView(), okCB, result);
    }

    /**
     * 启动位置信息监听
     * 如果成功,会持续执行JS成功回调;如果失败,直接执行JS失败回调
     *
     * @param params 插件对象,其包装了JS成功与失败回调方法名
     * @return 监听标识符,通过该标识符可以停止监听
     */
    public void startListen(PluginParams params) {
        params.showArguments();
        String okCB = params.getArguments()[0];
        WebviewUtil.execCallback(params.getWebView(), okCB, "Call back param");
    }

    /**
     * 停止位置监听
     *
     * @param params 插件对象,其包装了启动监听成功时返回的监听标识符
     * @return true:成功;false:失败
     */
    public boolean stopListen(PluginParams params) {
        String listenId = params.getArguments()[0];
        return true;
    }

    private String parseLocation(Location location) {
        Log.d(TAG, "location:" + location);
        if(location == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Provider:").append(location.getProvider())
                .append("\nLongitude:").append(location.getLongitude())
                .append("\nLatitude:").append(location.getLatitude())
                .append("\nAccuracy:").append(location.getAccuracy())
                .append("\nTime:").append(location.getTime());
        Log.d(TAG, "Location info:" + stringBuilder.toString());
        return stringBuilder.toString();
    }
}
