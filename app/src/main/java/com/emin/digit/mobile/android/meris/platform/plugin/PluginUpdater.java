package com.emin.digit.mobile.android.meris.platform.plugin;

import android.content.Context;

import com.emin.digit.mobile.android.meris.platform.core.PluginParams;
import com.emin.digit.mobile.android.meris.platform.components.appupdate.UpdateManager;

/**
 * Created by Samson on 2016/10/17.
 *
 * 更新功能插件
 */
public class PluginUpdater {

    private static final String TAG = PluginUpdater.class.getSimpleName();

    /**
     * apk方式更新app
     *
     * @param params 插件参数对象
     */
    public void updateApk(PluginParams params){
        final String httpUrl = params.getArguments()[0];
        UpdateManager.getInstance().updateApk(httpUrl);
    }

    /**
     * web模版资源更新
     *
     * @param params 插件参数对象
     */
    public void updateTemplate(PluginParams params){
        final String httpUrl = params.getArguments()[0];
        UpdateManager.getInstance().updateTemplate(httpUrl);
    }

    /**
     * 数据库的更新
     *
     * @param params
     */
    public boolean updateDatabase(PluginParams params){
        Context context = params.getWebView().getContext();
        String tablesJson = params.getArguments()[0];
        return UpdateManager.getInstance().updateDatabase(tablesJson);
    }
}
