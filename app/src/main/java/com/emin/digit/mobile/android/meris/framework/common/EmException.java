package com.emin.digit.mobile.android.meris.framework.common;

/**
 * Created by Samson on 16/7/26.
 */
public class EmException extends RuntimeException {


    public EmException() {
        super();
    }

    public EmException(String msg) {
        super(msg);
    }

    public EmException(Throwable ex) {
        super(ex);
    }

    public EmException(String msg, Throwable ex) {
        super(msg,ex);
    }
}
