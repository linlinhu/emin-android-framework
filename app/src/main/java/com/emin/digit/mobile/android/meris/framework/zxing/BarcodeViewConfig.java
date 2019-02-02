package com.emin.digit.mobile.android.meris.framework.zxing;

import android.graphics.Rect;

/**
 * Created by Samson on 2017/2/8.
 */
public class BarcodeViewConfig {

    public Rect rect = new Rect();

    private static BarcodeViewConfig instance;
    private BarcodeViewConfig(){
    }
    public static BarcodeViewConfig getInstance(){
        if(instance == null){
            instance = new BarcodeViewConfig();
        }
        return instance;
    }

    public static void clearData(){
        instance = null;
    }

    public Rect getBarcodeRect(){
        return rect;
    }


}
