package com.emin.digit.mobile.android.meris.platform.core;

import android.util.Log;
import android.webkit.WebView;

/**
 * 对Plugin方法参数的封装
 * 保证通过反射机制调用Plugin方法的时候,参数的统一
 * Javascript在传入参数可能是多个,通过数组的形式,调用EminBridge的执行方法时,
 * 这个参数数组对象对应,PluginParams的arguments
 *
 *
 * Created by Samson on 16/8/15.
 */
public class PluginParams {

    private WebView webView; // 加载网页的webView

    private String[] arguments; // js携带的参数

    public WebView getWebView() {
        return webView;
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    public void showArguments() {
        if(arguments == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(String str : arguments) {
            sb.append(str).append(",");
        }
        String str = sb.substring(0, sb.length() - 1) + "]";
        Log.d("PluginParams", "=== plugin arguments:" + str);
    }
}
