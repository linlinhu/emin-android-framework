package com.emin.digit.mobile.android.meris.platform.plugin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.emin.digit.mobile.android.meris.platform.core.EMHybridWebView;
import com.emin.digit.mobile.android.meris.platform.core.PluginParams;
import com.emin.digit.mobile.android.meris.platform.utils.DownloadUtil;
import com.emin.digit.mobile.android.meris.platform.utils.ZipUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Samson on 2016/10/14.
 *
 * 下载插件
 * 参考:http://www.jcodecraeer.com/a/chengxusheji/java/2017/0907/8484.html
 */
public class PluginDownloader {

    private static final String TAG = PluginDownloader.class.getSimpleName();

    private Activity activity;
    public void download(PluginParams params){
        final String httpUrl = params.getArguments()[0];
        //final String path = params.getArguments()[1];
        final String path = "/sdcard/meris";

        EMHybridWebView webView = (EMHybridWebView)params.getWebView();
        activity = webView.getActivity();

        // 不阻塞UI线程,启动子线程执行下载
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"download thread running.....");
                //String path = "/sdcard/meris";
                //Toast.makeText(activity, "apk下载完成,即将安装!", Toast.LENGTH_SHORT).show();
                final File file = DownloadUtil.downLoadFile(httpUrl,path);
            }
        }).start();
    }
}
