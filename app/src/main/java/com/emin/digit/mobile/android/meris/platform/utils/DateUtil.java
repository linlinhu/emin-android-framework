package com.emin.digit.mobile.android.meris.platform.utils;

import java.util.Date;

/**
 * Created by Samson on 2017/2/10.
 */
public class DateUtil {

    public static String currentDate(){
        return null;
    }

    public static String nanoTime() {
        long nanoTime = System.nanoTime();
        return String.valueOf(nanoTime);
    }

    public static String dateTimestamp(){
        long timestamp = new Date().getTime();
        return String.valueOf(timestamp);
    }
}
