package com.emin.digit.mobile.android.meris.platform.plugin;

import android.content.Context;

import com.emin.digit.mobile.android.meris.platform.components.permission.PermissionHelper;
import com.emin.digit.mobile.android.meris.platform.core.PluginParams;

/**
 * author: Samson
 * created on: 2017/11/7 下午 3:53
 * description:
 */
public class PluginPermission {

    private static final String TAG = PluginPermission.class.getSimpleName();

    public boolean checkWithName(PluginParams params) {
        params.showArguments();
        String name = params.getArguments()[0];
        Context context = params.getWebView().getContext();
        return PermissionHelper.getInstance(context).check(name);
    }
}
