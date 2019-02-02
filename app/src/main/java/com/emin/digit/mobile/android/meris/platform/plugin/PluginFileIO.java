package com.emin.digit.mobile.android.meris.platform.plugin;

import android.content.Context;

import com.emin.digit.mobile.android.meris.platform.core.PluginParams;
import com.emin.digit.mobile.android.meris.platform.utils.FileUtil;

/**
 * author: Samson
 * created on: 16/8/22
 * description:
 * 文件操作插件
 */
public class PluginFileIO {

    private static final String TAG = PluginFileIO.class.getSimpleName();

    /**
     * 获取assets目录下的文件的内容
     * 备注:assets目录在src/main下
     * apps/AppPage/www/JSON/client.json
     *
     * @param params
     * @return assets目录下的文件的内容
     */
    public static String readAssetsFile(PluginParams params) {
        String filePath = params.getArguments()[0];
        Context context = params.getWebView().getContext();
        return FileUtil.readAssetsFile(context, filePath);
    }
}
