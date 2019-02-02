package com.emin.digit.mobile.android.meris.platform.plugin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.emin.digit.mobile.android.meris.platform.core.EMHybridActivity;
import com.emin.digit.mobile.android.meris.platform.core.EMHybridWebView;
import com.emin.digit.mobile.android.meris.platform.core.PluginParams;

/**
 * Created by Samson on 16/8/16.
 */
public class PluginCamera {

    private static final String TAG = PluginCamera.class.getSimpleName();

    public void startCamera(PluginParams params){
        Log.d(TAG,"= = = startCamera");
        final EMHybridWebView webView = (EMHybridWebView) params.getWebView();
        final EMHybridActivity activity = (EMHybridActivity)webView.getActivity();

        webView.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction("com.emin.digit.test.TestCameraActivity");
                //用Bundle携带数据
                Bundle bundle=new Bundle();
                bundle.putString("methodName", "startCamera");
                intent.putExtras(bundle);
                activity.startActivity(intent);
            }
        });
    }
}
