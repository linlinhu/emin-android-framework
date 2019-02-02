package com.emin.digit.mobile.android.meris.framework.serviceRequest.test;

import android.util.Log;

import com.emin.digit.mobile.android.meris.framework.serviceRequest.okHttpRequest.OkHttpRequest;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * author: Samson
 * created on: 2017/11/17 上午 10:27
 * description:
 */
public class HeWeatherServiceImpl extends OkHttpRequest {

    private static final String TAG = HeWeatherServiceImpl.class.getSimpleName();

    public void send(final OkHttpCallback cb) {
        Log.d(TAG, "sending....1");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(IHeWeatherService.BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        IHeWeatherService service = retrofit.create(IHeWeatherService.class);

        String city = "beijing";
        String apiKey = "2600eec475b7415997dfa890a4cad497";
        //Observable<ResponseBody> s = service.searchCityInfo(city, apiKey);

        Consumer<ResponseBody> consumer = new Consumer<ResponseBody>() {
            @Override
            public void accept(ResponseBody responseBody) throws Exception {
                String response = responseBody.string();
                printThreadInfo();
                System.out.println("responseBody content:" + response);
                cb.onSuccess(response);
            }
        };

        // ObservableOnSubscribe
        Observable<ResponseBody> observable = Observable.create(new ObservableOnSubscribe<ResponseBody>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<ResponseBody> e) throws Exception {

            }
        });

        Subscriber<ResponseBody> subscriber = new Subscriber<ResponseBody>() {
            @Override
            public void onSubscribe(Subscription s) {
                Log.d(TAG,"onSubscribe " + s.toString());
                s.cancel();
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                try {
                    String response = responseBody.string();
                    printThreadInfo();
                    System.out.println("responseBody content:" + response);
                    cb.onSuccess(response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG,"onError");
            }

            @Override
            public void onComplete() {
                Log.d(TAG,"onComplete");
            }
        };

        service.searchCityInfo(city, apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(subscriber);
                .subscribe(consumer);
    }

    private String getThreadInfo() {
        StringBuilder stringBuilder = new StringBuilder()
                .append("当前线程信息:")
                .append("id:")
                .append(Thread.currentThread().getId())
                .append(", name:")
                .append(Thread.currentThread().getName());
        return stringBuilder.toString();
    }

    // 打印当前线程信息
    private void printThreadInfo() {
        Log.d(TAG, getThreadInfo());
    }
}
