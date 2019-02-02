package com.emin.digit.mobile.android.meris.framework.serviceRequest.okHttpRequest;

import android.util.Log;

import com.emin.digit.mobile.android.meris.framework.serviceRequest.okHttpRequest.interceptor.LoggingInterceptor;
import com.emin.digit.mobile.android.meris.framework.serviceRequest.okHttpRequest.interceptor.ParamsInterceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * author: Samson
 * created on: 2017/10/25 0025 上午 10:48
 * description:
 * 通过Service的类型构建service的实例
 */
public class ServiceFactory {

    private static final String TAG = ServiceFactory.class.getSimpleName();
    private final static String heWeatherApiUrl = "https://free-api.heweather.com/v5/";

    public static <T> T build(Class<T> clazz) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(heWeatherApiUrl)
                .build();
        return retrofit.create(clazz);
    }

    /**
     * 生成service请求示例
     *
     * 支持动态url(baseUrl/域名)
     *
     *
     * @param clazz
     * @param baseUrl
     * @param <T>
     * @return
     */
    public static <T> T buildGet(Class<T> clazz, String baseUrl) {
        return buildGet(clazz, baseUrl, null);
    }

    public static <T> T buildGet(Class<T> clazz, String baseUrl, Map<String, String> header) {
        Log.d(TAG, "== buildGet invoked");
        List<Interceptor> interceptors = new ArrayList<>();
        if(header != null) {
            ParamsInterceptor paramsInterceptor = new ParamsInterceptor.Builder()
                    .addHeaderParamsMap(header)
                    .build();
            interceptors.add(paramsInterceptor);
        }
        interceptors.add(new LoggingInterceptor());
        OkHttpClient client = HttpClientFactory.buildClient(interceptors, 10);
        return build(clazz, baseUrl, client);
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(baseUrl)
//                .client(client)
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .build();
//        return retrofit.create(clazz);
    }

    public static <T> T buildPost(Class<T> clazz, String baseUrl) {
        return buildPost(clazz, baseUrl, null);
    }

    public static <T> T buildPost(Class<T> clazz, String baseUrl, Map<String, String> header) {
        Log.d(TAG, "== buildPost invoked");
        List<Interceptor> interceptors = new ArrayList<>();
        if(header != null) {
            ParamsInterceptor paramsInterceptor = new ParamsInterceptor.Builder()
                    .addHeaderParamsMap(header)
                    .build();
            interceptors.add(paramsInterceptor);
        }
        interceptors.add(new LoggingInterceptor());
        OkHttpClient client = HttpClientFactory.buildClient(interceptors, 10);
        return build(clazz, baseUrl, client);
    }

    public static <T> T build(Class<T> clazz, String baseUrl, OkHttpClient client) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit.create(clazz);
    }
}
