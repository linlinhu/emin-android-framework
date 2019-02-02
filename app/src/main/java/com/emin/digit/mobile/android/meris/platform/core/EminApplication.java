package com.emin.digit.mobile.android.meris.platform.core;

import android.app.Application;
import android.util.Log;

import com.emin.digit.mobile.android.meris.platform.components.net.NetUtil;

/**
 * Created by Samson on 2017/1/11.
 */
public class EminApplication extends Application{
    private static final String TAG = EminApplication.class.getSimpleName();

    // singleton
    private static EminApplication instance;
    public static synchronized EminApplication getInstance() {
        return instance;
    }

    private EMHybridActivity mainActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"=== onCreate");
        instance = this;
    }

    public EMHybridActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(EMHybridActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
}
