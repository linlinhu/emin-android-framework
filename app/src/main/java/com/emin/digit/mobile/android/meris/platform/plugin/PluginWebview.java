package com.emin.digit.mobile.android.meris.platform.plugin;

import android.util.Log;
import android.webkit.WebView;

import com.emin.digit.mobile.android.meris.platform.core.EMHybridActivity;
import com.emin.digit.mobile.android.meris.platform.core.EMHybridWebView;
import com.emin.digit.mobile.android.meris.platform.core.EminApplication;
import com.emin.digit.mobile.android.meris.platform.core.PluginParams;
import com.emin.digit.mobile.android.meris.platform.utils.WebviewUtil;

import org.json.JSONObject;

import java.util.LinkedList;

/**
 * author: Samson
 * created on: 2017/1/11 下午 2:39
 * description:
 */
public class PluginWebview {

    private static final String TAG = PluginWebview.class.getSimpleName();

    public void currentWebview(PluginParams params){
        EMHybridWebView webview = (EMHybridWebView) params.getWebView();
    }

    public String getId(PluginParams params){
        EMHybridWebView webview = (EMHybridWebView) params.getWebView();
        return webview.getViewId();
    }

    /**
     * 获取界面跳转后传递的参数,key为extras
     *   例如:var extras = EminBridge.pluginWebview.getExtras();
     *       var extrasObj = JSON.stringify(extras);
     *       var name = extrasObj.username; // username是参数(json)中的key
     *
     * @param params
     * @return
     */
    public String getExtras(PluginParams params){
        EMHybridWebView webview = (EMHybridWebView) params.getWebView();
        JSONObject extras = webview.getExtras();
        return extras.toString();
    }

    public WebView getWebviewById(PluginParams params){
        String viewId = params.getArguments()[0];
        LinkedList<EMHybridWebView> list = EMHybridActivity.getWebViewList();
        EMHybridWebView target = null;
        for(int i = 0 ; i < list.size(); i++){
            EMHybridWebView wv = list.get(i);
            if(wv.getViewId().equals(viewId)){
                Log.d(TAG,"== find view id:" + viewId + " at index:" + i);
                target = wv;
                break;
            }
        }
        return target;
    }

    public void close(PluginParams params){
        String viewId = params.getArguments()[0];
        EMHybridActivity activity = EminApplication.getInstance().getMainActivity();

    }

    /**
     * 执行指定的界面的方法
     * 比如在返回某个指定的界面,并且执行该界面中的某个查询刷新界面
     *
     * @param params 插件参数对象
     */
    public void invokeMethod(PluginParams params){
        String viewId = params.getArguments()[0]; // 目标webview的id
        String methodName = params.getArguments()[1]; // 要调用的目标界面的方法
        String options = params.getArguments()[2]; // 要调用的目标界面的方法的参数,以JSON包装

        //EMHybridWebView webview = (EMHybridWebView)getWebview(viewId);

        final EMHybridWebView webView = (EMHybridWebView) params.getWebView();
        final EMHybridActivity activity = (EMHybridActivity)webView.getActivity();
        EMHybridWebView targetView = activity.getWebviewById(viewId);
        if(targetView == null) {
            Log.d(TAG,"=== webview with:" + viewId + " not found");
            return;
        }
        Log.d(TAG,"=== webview with:" + viewId + " found");
        WebviewUtil.execCallback(targetView,methodName,options);
    }

    public void callAnonymousFunction(PluginParams params) {
        params.showArguments();
        final String jsStr = params.getArguments()[0];
        final WebView webView = params.getWebView();

        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript" + jsStr);
            }
        });

    }

    private WebView getWebview(String id){
        EMHybridActivity activity = EminApplication.getInstance().getMainActivity();
        LinkedList<EMHybridWebView> list = activity.getWebViewList();
        EMHybridWebView target = null;
        for(int i = 0 ; i < list.size(); i++){
            EMHybridWebView wv = list.get(i);
            if(wv.getViewId().equals(id)){
                Log.d(TAG,"== find view id:" + id + " at index:" + i);
                target = wv;
                break;
            }
        }
        return target;
    }
}
