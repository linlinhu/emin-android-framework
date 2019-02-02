package com.emin.digit.mobile.android.meris.framework.serviceRequest;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * author: Samson
 * created on: 2017/10/26 上午 10:40
 * description:
 */
public interface IServiceRequest {

    /**
     * Get方式,通用形式service封装
     * path和参数均有外部传入配置
     * 其中:参数以map形式实现多参数,key即为服务端service方式的参数名,value为参数的值
     *
     * @param path baseUrl下的路劲
     * @param options 参数
     * @return
     */
    @GET("{path}")
    Observable<ResponseBody> get(@Path("path") String path,
                                 @QueryMap Map<String, String> options);


    @FormUrlEncoded
    @POST("{path}")
    Observable<ResponseBody> post(@Path("path") String path,
                                  @FieldMap Map<String, String> options);
}
