package com.emin.digit.mobile.android.meris.framework.database.exception;

/**
 * author: Samson
 * created on: 2017/11/15 0015 上午 11:53
 * description:
 */
public class IllegalPrimaryKeyException extends DatabaseException {
    public IllegalPrimaryKeyException() {
    }

    public IllegalPrimaryKeyException(String msg) {
        super(msg);
    }

    public IllegalPrimaryKeyException(Throwable ex) {
        super(ex);
    }

    public IllegalPrimaryKeyException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
