package com.emin.digit.mobile.android.meris.framework.database.exception;

/**
 * author: Samson
 * created on: 2017/11/2 0002 下午 2:05
 * description:
 */
public class UnknownTableException extends DatabaseException {
    private static final long serialVersionUID = 1L;

    public UnknownTableException() {
    }

    public UnknownTableException(String msg) {
        super(msg);
    }

    public UnknownTableException(Throwable ex) {
        super(ex);
    }

    public UnknownTableException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
