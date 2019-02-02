package com.emin.digit.mobile.android.meris.framework.serviceRequest.okHttpRequest.interceptor;

import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * author: Samson
 * created on: 2017/10/30 0030 上午 10:29
 * description:
 * 通过实现 okhttp3.Interceptor,实现自定义日志打印格式的拦截器
 */
public class LoggingInterceptor implements Interceptor {
    private static final String TAG = LoggingInterceptor.class.getSimpleName();

    private final Charset UTF8 = Charset.forName("UTF-8");

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();

        RequestBody requestBody = request.body();
        String body = null;
        if(requestBody != null) {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);

            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            body = buffer.readString(charset);
        }
        StringBuilder req = new StringBuilder();
        req.append("sending request:" + "\n");
        req.append("url:" + request.url() + "\n");
        req.append("method:" + request.method() + "\n");
        req.append("headers:" + request.headers().toString() + "\n");
        req.append("body:" + body);
        Log.d(TAG, "====" + req.toString());

        long startNs = System.nanoTime();
        Response response = chain.proceed(request);
        long coastMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        String rBody = null;
        if(responseBody != null) {
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer();

            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8);
                } catch (UnsupportedCharsetException e) {
                    e.printStackTrace();
                }
            }
            rBody = buffer.clone().readString(charset);
        }

        StringBuilder rsp = new StringBuilder();
        rsp.append("Received Response:" + "\n");
        rsp.append("url:" + response.request().url() + "\n");
        rsp.append("code:" + response.code() + "\n");
        rsp.append("message:" + response.message() + "\n");
        rsp.append("time:" + coastMs + "ms" + "\n");
        rsp.append("header:" + response.headers().toString() + "\n");
        rsp.append("response body:" + rBody);
        Log.d(TAG, "===" + rsp.toString());
        return response;
    }
}
