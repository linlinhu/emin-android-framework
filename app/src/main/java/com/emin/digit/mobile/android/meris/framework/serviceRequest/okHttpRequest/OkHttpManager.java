package com.emin.digit.mobile.android.meris.framework.serviceRequest.okHttpRequest;

import android.util.Log;
import com.emin.digit.mobile.android.meris.platform.utils.UUIDGenerator;
import java.util.HashMap;
import java.util.Map;

/**
 * author: Samson
 * created on: 2017/10/30 下午 4:12
 * description:
 */
public class OkHttpManager {

    private static final String TAG = OkHttpManager.class.getSimpleName();

    private Map<String, OkHttpRequest> requestMap;

    private static volatile OkHttpManager instance = null;

    private OkHttpManager() {
        requestMap = new HashMap<>();
    }

    public static OkHttpManager getInstance() {
        if (instance == null) {
            synchronized (OkHttpManager.class) {
                if (instance == null) {
                    instance = new OkHttpManager();
                }
            }
        }
        return instance;
    }

    /**
     * 创建Get请求
     *
     * @param baseUrl  url
     * @param path     path
     * @param data     数据
     * @param callback 回调
     * @return 本次请求的标识
     */
    public String sendRequestGet(String baseUrl, String path, Map<String, String> data,
                                 OkHttpRequest.OkHttpCallback callback) {
        return sendRequestGet(baseUrl, path, data, null, callback);
    }

    public String sendRequestGet(String baseUrl, String path, Map<String, String> data,
                                 Map<String, String> header, OkHttpRequest.OkHttpCallback callback) {
        OkHttpRequest okHttpRequest = new OkHttpRequest();
        if (header == null || header.isEmpty()) {
            okHttpRequest.doGet(baseUrl, path, data, callback);
        } else {
            okHttpRequest.doGet(baseUrl, path, data, header, callback);
        }
        return generateRequestId(okHttpRequest);
    }

    /**
     * 创建无header的post请求
     *
     * @param baseUrl  url
     * @param path     path
     * @param data     数据
     * @param callback 回调
     * @return 本次请求的标识
     */
    public String sendRequestPost(String baseUrl, String path, Map<String, String> data,
                                  OkHttpRequest.OkHttpCallback callback) {
        return sendRequestPost(baseUrl, path, data, null, callback);
    }

    /**
     * 创建有header的post请求
     *
     * @param baseUrl  url
     * @param path     path
     * @param data     数据
     * @param header   post请求的header
     * @param callback 回调
     * @return 本次请求的标识
     */
    public String sendRequestPost(String baseUrl, String path, Map<String, String> data,
                                  Map<String, String> header, OkHttpRequest.OkHttpCallback callback) {
        OkHttpRequest okHttpRequest = new OkHttpRequest();
        if (header == null || header.isEmpty()) {
            okHttpRequest.doPost(baseUrl, path, data, callback);
        } else {
            okHttpRequest.doPost(baseUrl, path, data, header, callback);
        }
        return generateRequestId(okHttpRequest);
    }

    /**
     * 取消请求
     * 备注:取消请求后,将不再关注请求的结果
     *
     * @param requestId 请求的标识
     */
    public void cancel(String requestId) {
        OkHttpRequest okHttpRequest = requestMap.get(requestId);
        if (okHttpRequest != null) {
            okHttpRequest.cancel();
            requestMap.remove(requestId);
        }
    }

    private String generateRequestId(OkHttpRequest request) {
        String requestId = "request_id_" + UUIDGenerator.genUUID(16);
        Log.d(TAG, "=== " + requestId);
        requestMap.put(requestId, request);
        return requestId;
    }
}
