package com.emin.digit.mobile.android.meris.platform.plugin;

import android.text.TextUtils;
import android.util.Log;

import com.emin.digit.mobile.android.meris.framework.serviceRequest.okHttpRequest.OkHttpManager;
import com.emin.digit.mobile.android.meris.framework.serviceRequest.okHttpRequest.OkHttpRequest;
import com.emin.digit.mobile.android.meris.platform.core.EMHybridWebView;
import com.emin.digit.mobile.android.meris.platform.core.PluginParams;
import com.emin.digit.mobile.android.meris.platform.utils.JSUtil;
import com.emin.digit.mobile.android.meris.platform.utils.WebviewUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.reactivex.annotations.NonNull;

/**
 * author: Samson
 * created on: 2017/10/25 下午 2:39
 * description:
 * 网络请求插件api
 * 1.提供get方式网络请求
 * 2.提供post方式网络请求
 */
public class PluginOkHttpRequest {

    private static final String TAG = PluginOkHttpRequest.class.getSimpleName();

    /**
     * get网络请求
     *
     * @param params 插件参数对象,其包装web前端网络请求的配置JSON字符串
     *               例:{
     *                  url:'http://192.168.0.202:8881/api-rdg/li/',
     *                  path:'findNotReceivedGoods',
     *                  data:{
     *                      page:1,
     *                      limit:10
     *                  },
     *                  header:{
     *                      ecmId:123
     *                  },
     *                  type:'get',
     *                  success:function(result){
     *                      alert(result);
     *                  },
     *                  error:function(e){
     *                      alert('error:' + e.message);
     *                  }
     *               }
     *
     * @return 本次网络请求标识
     */
    public String get(PluginParams params) {
        JSONObject options = JSUtil.jsonObjectFromString(params.getArguments()[0]);
        if(options == null) {
            return "";
        }
        final String url = options.optString("url");
        final String path = options.optString("path");
        String header = options.optString("header");
        final String data = options.optString("data");
        final String okCallBack = options.optString("success");
        final String errorCallBack = options.optString("error");

        final EMHybridWebView webview = (EMHybridWebView)params.getWebView();
        Map<String, String> pMap = jsonToMap(data);
        Map<String, String> headerMap = jsonToMap(header); // header解析
        OkHttpManager mgr = OkHttpManager.getInstance();
        String requestId = mgr.sendRequestGet(url, path, pMap, headerMap, new OkHttpRequest.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i(TAG,"== get onSuccess response:" + response);
                WebviewUtil.execAnonymousFunc(webview, okCallBack, response);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.i(TAG,"== get onError:" + e.getMessage());
                String msg = JSUtil.wrapKeyValue("message", e.getMessage());
                WebviewUtil.execAnonymousFunc(webview, errorCallBack, msg);
            }
        });
        Log.d(TAG, "== get request id:" + requestId);
        return requestId;
    }

    /**
     * post网络请求
     *
     * @param params 插件参数对象,其包装web前端网络请求的配置JSON字符串
     *               例:{
     *                  url:'http://192.168.0.202:8881/api-user/',
     *                  path:'clientLogin',
     *                  data:{username:'admin', password:'123456'},
     *                  header:{
     *                    token:'abc123'
     *                  },
     *                  type:'post',
     *                  success:function(result){
     *                       alert(result);
     *                  },
     *                  error:function(e){
     *                       alert('error:' + e.message);
     *                  }
     *               }
     *
     * @return 本次网络请求标识
     */
    public String post(PluginParams params) {
        JSONObject options = JSUtil.jsonObjectFromString(params.getArguments()[0]);
        if(options == null) {
            return "";
        }
        final String url = options.optString("url");
        final String path = options.optString("path");
        String header = options.optString("header");
        final String data = options.optString("data");
        final String okCallBack = options.optString("success");
        final String errorCallBack = options.optString("error");

        final EMHybridWebView webview = (EMHybridWebView)params.getWebView();
        Map<String, String> paramMap = jsonToMap(data);   // 请求参数数据解析
        Map<String, String> headerMap = jsonToMap(header); // header解析
        OkHttpManager mgr = OkHttpManager.getInstance();
        String requestId = mgr.sendRequestPost(url, path, paramMap, headerMap, new OkHttpRequest.OkHttpCallback() {
            @Override
            public void onSuccess(final String response) {
                Log.d(TAG,"== doPost onSuccess response:" + response);
                WebviewUtil.execAnonymousFunc(webview, okCallBack, response);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
                Log.d(TAG,"== doPost onError:" + e.getMessage());
                String msg = JSUtil.wrapKeyValue("message", e.getMessage());
                WebviewUtil.execAnonymousFunc(webview, errorCallBack, msg);
            }
        });
        Log.d(TAG, "== post request id:" + requestId);
        return requestId;
    }

    public void cancel(PluginParams params) {
        String requestId = params.getArguments()[0];
        OkHttpManager.getInstance().cancel(requestId);
    }

    // Json字符串转成Map
    private Map<String, String> jsonToMap(String jsonString) {
        if(TextUtils.isEmpty(jsonString)) {
            return null;
        }
        try {
            JSONObject json = new JSONObject(jsonString);
            Iterator<String> keys = json.keys();
            Map<String, String> map = new HashMap<>();
            while (keys.hasNext()) {
                String key = keys.next();
                map.put(key,json.optString(key));
            }
            return map;
        } catch (JSONException e) {
            Log.e(TAG, "JSONException:" + e);
        }
        return null;
    }

}
