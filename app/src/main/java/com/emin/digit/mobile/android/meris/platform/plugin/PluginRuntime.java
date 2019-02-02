package com.emin.digit.mobile.android.meris.platform.plugin;

import com.emin.digit.mobile.android.meris.platform.core.EMHybridActivity;
import com.emin.digit.mobile.android.meris.platform.core.EMHybridWebView;
import com.emin.digit.mobile.android.meris.platform.core.PluginParams;

/**
 * author: Samson
 * created on: 2017/12/13 上午 10:11
 * description:
 */
public class PluginRuntime {

    public void exit(PluginParams params) {
        EMHybridWebView webView = (EMHybridWebView) params.getWebView();
        EMHybridActivity activity = (EMHybridActivity)webView.getActivity();
        activity.finish();
        System.exit(0);
    }
}
