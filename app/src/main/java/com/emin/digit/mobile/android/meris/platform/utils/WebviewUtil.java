package com.emin.digit.mobile.android.meris.platform.utils;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

/**
 * author: Samson
 * created on: 2017/1/21
 * description:
 * WebView工具
 */
public class WebviewUtil {
    private static final String TAG = "WebviewUtil";
    /**
     * 执行js方法
     * 参数统一用JSON才包装,因为参数的个数不确定,界面的方法参数比如是e,则e.param1,e.param2,e.paramN来获取
     *
     * @param webView 加载html的界面webview
     * @param callbackName js方法
     * @param paramJson js方法的参数,是个以JSON包装的对象String
     */
    public static void execCallback(final WebView webView,
                                    final String callbackName, final String paramJson) {
        String code;
        if(TextUtils.isEmpty(paramJson)) {
            code = "javascript:" + callbackName + "()";
        } else {
            code = "javascript:" + callbackName + "('" + paramJson + "')";
        }
        final String jsCode = code;
        webView.post( new Runnable() {
            @Override
            public void run() {
                webView.loadUrl(jsCode);
            }
        });
    }

    /**
     * 执行匿名函数回调
     * 将匿名函数如: function(param){alert(param);}
     * 构建成 funcId = function(param){alert(param);}(param) 方式执行
     *
     * @param webView 界面webView
     * @param anonymousFunction 匿名函数体
     * @param param 匿名函数的参数
     */
    public static void execAnonymousFunc(final WebView webView,
                                         final String anonymousFunction,
                                         final String param) {
        StringBuilder stringBuilder = new StringBuilder();
        String funcName = genJavascriptFunctionId();
        stringBuilder.append(funcName).append("=").append(anonymousFunction).append("(");
        if(!TextUtils.isEmpty(param)) {
            stringBuilder.append(param);
        }
        stringBuilder.append(")");
        final String jsCode = "javascript:" + stringBuilder.toString();
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl(jsCode);
            }
        });
    }

    /**
     * 生成WebView的唯一性标识
     *
     * @return WebView唯一标识
     */
    public static String genWebViewId() {
        String id = UUIDGenerator.genUUID(16);
        return "webview_id_" + id;
    }

    /**
     * 生成javascript的function的标识(方法名)
     * 在匿名函数的时候,webView不能直接执行该函数体,需要构建一个可执行的函数表达式并且执行
     *
     * @return javascript函数的唯一标识
     */
    public static String genJavascriptFunctionId() {
        String id = UUIDGenerator.genUUID(16);
        return "function_id_" + id;
    }
}
