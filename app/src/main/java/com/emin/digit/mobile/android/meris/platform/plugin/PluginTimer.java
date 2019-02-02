package com.emin.digit.mobile.android.meris.platform.plugin;

import android.content.Context;
import android.util.Log;

import com.emin.digit.mobile.android.meris.platform.components.timer.TimerManager;
import com.emin.digit.mobile.android.meris.platform.core.PluginParams;

/**
 * author: Samson
 * created on: 2016/10/28 上午 11:23
 * description:
 * Timer插件
 */
public class PluginTimer {

    private static final String TAG = PluginTimer.class.getSimpleName();

    public void startup(PluginParams params) {
        Context context = params.getWebView().getContext();
        TimerManager.setup(context);
    }

    public void createTask(PluginParams params) {
        String taskName = params.getArguments()[0];
        TimerManager.getInstance().createTask(taskName);
    }

    public boolean cancelTask(PluginParams params) {
        Log.d(TAG,"cancel");
        String taskId = params.getArguments()[0];
        return TimerManager.getInstance().cancelTask(taskId);
    }
}
