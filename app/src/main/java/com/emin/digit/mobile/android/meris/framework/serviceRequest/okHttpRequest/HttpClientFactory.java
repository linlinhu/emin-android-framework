package com.emin.digit.mobile.android.meris.framework.serviceRequest.okHttpRequest;

import com.emin.digit.mobile.android.meris.framework.serviceRequest.okHttpRequest.interceptor.LoggingInterceptor;

import java.io.IOException;
import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * author: Samson
 * created on: 2017/10/27 0027 下午 4:13
 * description:
 * HttpClient工厂,由于Http框架较多,OkHttp是其中一种
 */
public class HttpClientFactory {

    private static final String TAG = HttpClientFactory.class.getSimpleName();

    private static final long TIME_OUT = 1000;

    private static final String CONNECT_TIME_OUT = "timeout";

    public static OkHttpClient buildClient(List<Interceptor> interceptors, long timeOut) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if(interceptors != null) {
            for(Interceptor interceptor : interceptors) {
                builder.addInterceptor(interceptor);
            }
        }
        builder.connectTimeout(timeOut, TimeUnit.SECONDS);
        return builder.build();
    }

    public static OkHttpClient buildClient(List<Interceptor> interceptors, Map<Key, Object> options) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if(interceptors != null) {
            for(Interceptor interceptor : interceptors) {
                builder.addInterceptor(interceptor);
            }
        }
        if(options != null) {
            long timeout = options.containsKey(CONNECT_TIME_OUT) ? (long)options.get(CONNECT_TIME_OUT) : 10;
            builder.connectTimeout(timeout, TimeUnit.SECONDS);
        }
        return builder.build();
    }

    /**
     * 默认的client
     *
     * @return
     */
    public static OkHttpClient defaultClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(String s) {
                        // Log.d(TAG, "==== getOkHttpClient log:" + s);
                    }
                }))
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                .build();
        return client;
    }

    /**
     * 实现httpclient中在http request中添加 Header
     *
     * @param headerMap header的Map(key-value)
     * @return
     */
    public static OkHttpClient getOkHttpClient(final Map<String, String> headerMap) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        Request.Builder builder = request.newBuilder();
                        Set<String> keys = headerMap.keySet();
                        for(String key : keys) {
                            String value = headerMap.get(key);
                            builder.addHeader(key, value);
                        }
                        return chain.proceed(builder.build());
                    }
                })
                .build();
        return client;
    }
}
