package com.emin.digit.mobile.android.meris.platform.core;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.emin.digit.mobile.android.meris.platform.components.redot.ReddotManager;
import com.emin.digit.mobile.android.meris.platform.plugin.PluginAlert;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * Web前端与原生层交互的桥梁
 * HTML(javascript)统一通过此被注入到WebView的对象,来实现对原生Java类方法的调用
 *
 * Created by Samson on 16/8/11.
 * 变更履历:
 * 2016/10/17 废弃执行插件的方法,移植到通过PluginManager去调用
 * 2016-11-02 template引擎API追加
 * 2017-02-13 追加openWindow API,可以传递自定义的参数等,取代原来的loadWebPage API
 * 2017-02-13 追加popToWindow API,可以返回的到指定的id的界面,之前只有返回上一级
 */
public class EMBridge {

    private static final String TAG = EMBridge.class.getSimpleName();
    private Context mContext; // 上下文
    private WebView mWebView;

    public EMBridge(Context context, WebView webView) {
        mContext = context;
        mWebView = webView;
    }

    /**
     * 2016-10-13 Add 取代原来直接在EminBridge中执行插件,采用PluginManager来负责
     *
     * @param pluginName 插件的名字(js中所采用的),在PluginManager中配置所关联的具体的类
     * @param methodName 执行的插件方法名
     * @param args js传入的参数
     * @return 执行的结果
     */
    @JavascriptInterface
    public String executePlugin(String pluginName, String methodName, String[] args) {
        return PluginManager.getInstance(mWebView).execSyncPlugin(pluginName, methodName, args);
    }

    /**
     * 2016-10-13 Add 取代原来直接在EminBridge中执行插件,采用PluginManager来负责
     *
     * @param pluginName 插件的名字(js中所采用的),在PluginManager中配置所关联的具体的类
     * @param methodName 执行的插件方法名
     * @return 执行的结果
     */
    @JavascriptInterface
    public String executePlugin(String pluginName, String methodName) {
        return PluginManager.getInstance(mWebView).execSyncPlugin(pluginName, methodName, null);
    }

    /**
     * 异步执行plugin
     *
     * @param pluginName 插件名称(在PluginManager中注册的名字)
     * @param methodName 插件方法名称
     * @param args 参数数组
     */
    @JavascriptInterface
    public void execPlugin(String pluginName, String methodName, String[] args) {
        Object result = null;
        // 通过pluginName找到JAVA类,并执行method
        try {
            final Class pluginClass = Class.forName(pluginName);
            final Method method = pluginClass.getDeclaredMethod(methodName, String.class);
            final String[] arguments = args;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        PluginParams params = new PluginParams();
                        params.setWebView(mWebView);
                        if (arguments != null) {
                            params.setArguments(arguments);
                        }
                        method.invoke(pluginClass.newInstance(), params);
                    } catch (InstantiationException e) {
                        Log.e(TAG, e.getMessage());
                    } catch (IllegalAccessException e) {
                        Log.e(TAG, e.getMessage());
                    } catch (InvocationTargetException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }).start();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开新的webview展现web内容
     *
     * @param jsonOptions 参数配置json
     *                    例如
     *                    var options = {url:'send/index.html',id:'index'};
     *                    EminBridge.openWindow(JSON.stringify(options));
     */
    @JavascriptInterface
    public void openWindow(String jsonOptions) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonOptions);
        } catch(JSONException e) {
            Log.e(TAG, "openWindow JSON参数异常:" + e.getMessage());
            return;
        }
        final String url = jsonObject.optString("url");
        if(TextUtils.isEmpty(url)){
            return;
        }
        final EMHybridActivity activity = (EMHybridActivity)mContext;
        final JSONObject options = jsonObject;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.loadContentWebview(options);
            }
        });
    }

    /**
     * 返回到目标的界面,不一定是当前的上一级,可以是当前界面的上 n 级的界面
     *
     * @param id 目标界面的id
     */
    @JavascriptInterface
    public void popToWindow(String id){
        final EMHybridActivity activity = (EMHybridActivity)mContext;
        final int index = activity.getWebviewIndexById(id);
        if(index == -1) {return;}
        Log.d(TAG,"==== index:" + index);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.popWebviewFromIndex(index);
            }
        });
    }

    @JavascriptInterface
    public void currentWebview(){
        EMHybridWebView webview = (EMHybridWebView) mWebView;
        String id = webview.getViewId();
        JSONObject extras = webview.getExtras();
        if(extras != null){
            Iterator<String> it = extras.keys();
            while (it.hasNext()){
                String key = it.next();
                String value = extras.optString(key);
            }
        }
    }

    /**
     * 返回
     */
    @JavascriptInterface
    public void back(){
        final EMHybridActivity activity = (EMHybridActivity)mContext;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK));
                activity.willToLastWebview();
            }
        });
    }

    @JavascriptInterface
    public void toast(String text) {
        new PluginAlert().toast(mContext,text);
    }

    @JavascriptInterface
    public void backWithCallback(final String functionName, final String param) {
        EMHybridWebView currentView = (EMHybridWebView)mWebView;
        EMHybridActivity activity = (EMHybridActivity)currentView.getActivity();

        List<EMHybridWebView> list = EMHybridActivity.getWebViewList();
        final EMHybridWebView lastView = list.get(list.size() - 2 );
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lastView.loadUrl("javascript:"+ functionName +"('" + param +"')");
            }
        });
    }

    // 红点服务相关
    // 无回调
    @JavascriptInterface
    public void reddotRegister(){
        ReddotManager.getInstance().register(mWebView);
    }

    // callback为注册的回调方法名
    @JavascriptInterface
    public void reddotRegister(String callback){
        ReddotManager.getInstance().register(mWebView,callback);
    }

    // 注销红点服务
    @JavascriptInterface
    public void reddotUnregister(String callback){
        ReddotManager.getInstance().unRegister(mWebView);
    }

    // 因为没有后台service推送交互，临时在界面js做个方法触发
    @JavascriptInterface
    public void unreadMessageComing(String msgJson) throws Exception {
        ReddotManager.getInstance().reddotCounting(msgJson);
    }

    @JavascriptInterface
    public void readMessageComing(String msgJson) throws Exception {
        ReddotManager.getInstance().reddotCounting(msgJson);
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }
}
