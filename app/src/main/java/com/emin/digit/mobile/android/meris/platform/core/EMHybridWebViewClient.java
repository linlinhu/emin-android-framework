package com.emin.digit.mobile.android.meris.platform.core;

import android.graphics.Bitmap;
import android.os.Message;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.emin.digit.mobile.android.meris.platform.core.EMHybridWebView.ViewType;

/**
 * author: Samson
 * created on: 2016/8/11
 * description:
 */
public class EMHybridWebViewClient extends WebViewClient {

    private static final String TAG = EMHybridWebViewClient.class.getSimpleName();

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        //Log.d(TAG,"shouldOverrideUrlLoading :" + url);
        // another Activity that handles URLs
        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        //startActivity(intent);
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.d(TAG,"== onPageStarted :" + url);
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.d(TAG,"== onPageFinished url:" + url);
        EMHybridWebView webView = (EMHybridWebView)view;
        EMHybridActivity activity = (EMHybridActivity)webView.getActivity();
        Message msg = Message.obtain();
        switch (webView.getViewType()){
            // 初始化界面,不要加显示出来的html
            case INIT:{
                msg.what = ViewType.INIT.ordinal();
                break;
            }
            // 普通内容画面类型
            case CONTENT:{
                msg.what = ViewType.CONTENT.ordinal();
                break;
            }
            default:{
                break;
            }
        }
        activity.webViewHandler.sendMessage(msg);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        Log.d(TAG,"==onLoadResource :" + url);
        super.onLoadResource(view, url);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        Log.d(TAG,"==onReceivedError");
        super.onReceivedError(view, request, error);
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        Log.d(TAG,"==onReceivedHttpError");
        super.onReceivedHttpError(view, request, errorResponse);
    }
}
