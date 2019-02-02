package com.emin.digit.mobile.android.meris.platform.plugin;

import android.util.Log;
import android.webkit.WebView;

import com.emin.digit.mobile.android.meris.platform.components.net.NetBroadcastReceiver;
import com.emin.digit.mobile.android.meris.platform.components.net.NetUtil;
import com.emin.digit.mobile.android.meris.platform.core.EMHybridActivity;
import com.emin.digit.mobile.android.meris.platform.core.EMHybridWebView;
import com.emin.digit.mobile.android.meris.platform.core.PluginParams;

/**
 * 网络相关插件
 * 1.为web界面新增网络监听(通过传入回调方法名称,自动执行js方法)
 *
 * Created by Samson on 2017/1/17.
 */
public class PluginNetwork{
    private static final String TAG = PluginNetwork.class.getSimpleName();

    /**
     * 注册网络监听
     *
     * @param params 插件参数对象
     */
    public void addNetStateListener(PluginParams params){
        EMHybridWebView webview = (EMHybridWebView)params.getWebView();
        String callbackName = params.getArguments()[0];
        webview.setNetStateRegister(true);
        webview.setNetStateCallback(callbackName);
        Log.d(TAG,"==== callbackName:" + callbackName);
        NetBroadcastReceiver.addListener(webview);
    }

    /**
     * 移除网络监听
     *
     * @param params 插件参数对象
     */
    public void removeNetStateListener(PluginParams params){
        EMHybridWebView webview = (EMHybridWebView)params.getWebView();
        webview.setNetStateRegister(false);
        NetBroadcastReceiver.removeListener(webview);
    }

    /**
     * 获取网络当前状态
     *
     * @param params 插件参数对象
     * @return 当前网络状态
     */
    public int getNetworkState(PluginParams params){
        // WebView webview = params.getWebView();
        final EMHybridWebView webView = (EMHybridWebView) params.getWebView();
        final EMHybridActivity activity = (EMHybridActivity)webView.getActivity();
        int networkState = NetUtil.getNetWorkState(activity.getApplication().getApplicationContext());
        return networkState;
    }
}
