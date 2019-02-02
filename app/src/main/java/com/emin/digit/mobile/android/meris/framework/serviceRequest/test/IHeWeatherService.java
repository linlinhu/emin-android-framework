package com.emin.digit.mobile.android.meris.framework.serviceRequest.test;

import com.emin.digit.mobile.android.meris.framework.serviceRequest.IServiceRequest;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * author: Samson
 * created on: 2017/10/24 0024 下午 4:54
 * description:
 */
public interface IHeWeatherService extends IServiceRequest {

    final static String BASE_URL = "https://free-api.heweather.com/v5/";

    /**
     * 获取城市信息
     *
     * @param city
     * @param apiKey
     * @return
     */
    @GET("search")
    Call<ResponseBody> getCityInfo(@Query("city") String city, @Query("key") String apiKey);

    /**
     * 获取城市信息
     * 与RxJava结合使用的service接口信息
     *
     * @param city
     * @param apiKey
     * @return
     */
    @GET("search")
    Observable<ResponseBody> searchCityInfo(@Query("city") String city, @Query("key") String apiKey);

    /**
     * 示例 https://free-api.heweather.com/v5/search?city=chengdu&key=2600eec475b7415997dfa890a4cad497
     *
     * 其组成部分:
     * baseUrl:https://free-api.heweather.com/v5/
     * path:search
     * 参数:city、key
     *
     * 备注:虽然path可以传入,但是参数的名称是固定的了(city和key),不具有通用性
     *
     * @param path
     * @param city
     * @param apiKey
     * @return
     */
    @GET("{path}")
    Observable<ResponseBody> searchCityInfo(@Path("path") String path,
                                            @Query("city") String city,
                                            @Query("key") String apiKey);

}
