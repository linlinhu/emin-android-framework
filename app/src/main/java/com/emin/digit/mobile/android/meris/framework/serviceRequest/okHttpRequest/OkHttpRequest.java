package com.emin.digit.mobile.android.meris.framework.serviceRequest.okHttpRequest;

import android.util.Log;

import com.emin.digit.mobile.android.meris.framework.serviceRequest.IServiceRequest;
import com.emin.digit.mobile.android.meris.framework.serviceRequest.test.IHeWeatherService;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * author: Samson
 * created on: 2017/10/25 下午 4:23
 * description:
 */
public class OkHttpRequest {

    private static final String TAG = OkHttpRequest.class.getSimpleName();

    public interface OkHttpCallback {
        void onSuccess(String response);
        void onError(@NonNull Throwable e);
    }

    private Disposable mDisposable;

    public Disposable getDisposable() {
        return mDisposable;
    }

    /**
     * Get请求
     * 多参数采用Map形式,前端输入采用JSON可以方便构建
     * 备注:
     * RxJava2.0开始subscribe()的对象(参数)不能是Subscriber,只能是Observer和Consumer
     *
     * @param baseUrl  服务url
     * @param path     服务path
     * @param data     参数数据
     * @param callback 接口回调
     */
    public void doGet(String baseUrl, String path,
                      Map<String, String> data, final OkHttpCallback callback) {
        doGet(baseUrl, path, data, null, callback);
    }

    public void doGet(String baseUrl, String path,
                      Map<String, String> data, Map<String, String> header,
                      final OkHttpCallback callback) {
        // 创建请求接口对象
        IServiceRequest serviceRequest = ServiceFactory.buildGet(IServiceRequest.class, baseUrl, header);
        // 创建get请求
        Observable<ResponseBody> observable= serviceRequest.get(path, data);
        // 创建监听网络请求过程的观察者
        Observer<ResponseBody> observer = getRequestObserver(callback);
        // 发送请求并监听结果
        configRequestObservable(observable).subscribe(observer);
    }

    /**
     * Post请求
     *
     * @param baseUrl  网络请求url
     * @param path     网络请求path
     * @param data     网络请求参数数据
     * @param header   网络请求请求header
     * @param callback 接口回调
     */
    public void doPost(String baseUrl, String path, Map<String, String> data,
                       Map<String, String> header, final OkHttpCallback callback) {
        // 创建请求接口对象
        IServiceRequest serviceRequest = ServiceFactory.buildPost(IServiceRequest.class, baseUrl, header);
        // 创建Post请求
        Observable<ResponseBody> observable = serviceRequest.post(path, data);
        // 创建监听网络请求过程的观察者
        Observer<ResponseBody> observer = getRequestObserver(callback);
        // 发送请求并监听结果
        configRequestObservable(observable).subscribe(observer);
    }

    /**
     * Post请求-无header
     *
     * @param baseUrl  网络请求url
     * @param path     网络请求path
     * @param data     网络请求参数数据
     * @param callback 接口回调
     */
    public void doPost(String baseUrl, String path,
                       Map<String, String> data, final OkHttpCallback callback) {
        doPost(baseUrl, path, data, null, callback);
    }

    public void cancel() {
        if(mDisposable != null ) {
            if(!mDisposable.isDisposed()) {
                mDisposable.dispose();
            }
        }
    }

    /**
     * 创建观察者
     * 负责对对网络请求的情况进行监听,并且将结果(响应的body)以接口回调的形式实现反馈
     *
     * TODO: 2017/11/17/ 能否对header也进行封装反馈?存在从header获取数据的情况
     *
     * @param callback OkHttpCallback回调接口
     * @return Observer
     */
    private Observer<ResponseBody> getRequestObserver(final OkHttpCallback callback) {
        // 如果需要完整的处理、过程复杂的,需要创建一个Observer
        Observer<ResponseBody> observer = new Observer<ResponseBody>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                mDisposable = d;
                Log.d(TAG, "==== observer onSubscribe d.isDisposed()" + d.isDisposed() + " " + getThreadInfo());
            }

            @Override
            public void onNext(@NonNull ResponseBody responseBody) {
                try {
                    String response = responseBody.string();
                    Log.d(TAG, "==== observer onNext" + getThreadInfo());
                    callback.onSuccess(response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.d(TAG, "==== observer onError" + e.getMessage() + " "  + getThreadInfo());
                callback.onError(e);
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "==== observer onComplete "  + getThreadInfo());
            }
        };
        return observer;
    }

    private Observable<ResponseBody> configRequestObservable(Observable<ResponseBody> observable) {
        observable.unsubscribeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG,"==== observable doOnDispose run "  + getThreadInfo());
                    }
                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        Log.d(TAG, "==== observable doOnSubscribe Thread info:" + getThreadInfo());
                    }
                })
                .doOnNext(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) throws Exception {
                        Log.d(TAG, "=== observable doOnNext: do something before Observer OnNext  Thread info:" + getThreadInfo());
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG,"==== observable doOnComplete run " + getThreadInfo());
                    }
                });
        return observable;
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
