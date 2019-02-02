package com.emin.digit.mobile.android.meris.framework.serviceRequest.okHttpRequest.interceptor;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * author: Samson
 * created on: 2017/10/30 0030 上午 11:12
 * description:
 * 日志拦截器实现
 */
public class LogInterceptor implements Interceptor {

    private static final String TAG = LogInterceptor.class.getSimpleName();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Log.d(TAG, "==== request:" + request.toString());
        long tStart = System.nanoTime();
        okhttp3.Response response = chain.proceed(chain.request());
        long tEnd = System.nanoTime();
        long millis = TimeUnit.NANOSECONDS.toMillis(tEnd - tStart);

        okhttp3.MediaType mediaType = response.body().contentType();
        String content = response.body().string();

        StringBuilder rsp = new StringBuilder();
        rsp.append("Received response for:" + response.request().url() + "\n");
        rsp.append("time coast:" + millis + "\n");
        rsp.append("header:" + response.headers() + "\n");
        rsp.append("body:" + content + "\n");

        Log.d(TAG, rsp.toString());
        return response.newBuilder()
                .body(okhttp3.ResponseBody.create(mediaType, content))
                .build();

    }
}
