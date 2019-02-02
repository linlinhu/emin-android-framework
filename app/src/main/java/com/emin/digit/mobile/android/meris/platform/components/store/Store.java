package com.emin.digit.mobile.android.meris.platform.components.store;

/**
 * author: Samson
 * created on: 2017/11/9 下午 5:57
 * description:
 */
public interface Store<T> {
    T get(String key);
    void put(String key, T value);
    void remove(String key);
}