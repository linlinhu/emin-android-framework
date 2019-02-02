package com.emin.digit.mobile.android.meris.platform.components.redot;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.emin.digit.mobile.android.meris.platform.core.EMHybridActivity;
import com.emin.digit.mobile.android.meris.platform.core.EMHybridWebView;

import org.json.JSONException;

import java.util.List;

/**
 * Created by Samson on 2016/10/11.
 *
 * 红点管理器,负责界面红点服务的注册/注销;
 * 红点数量的统计,并通过红点消息管理器(ReddotMessageManager),对数据库数据的更新
 */
public class ReddotManager {

    private static final String TAG = ReddotManager.class.getSimpleName();

    private EMHybridActivity activity;
    private static final String DEFAULT_JS_CALLBACK = "onReceiveReddot";

    private static ReddotManager instance = new ReddotManager();

    public static ReddotManager getInstance() {
        return instance;
    }

    private ReddotManager() {
    }

    // TODO: 2016/10/24 activity的初始化问题,Plugin方式进入,通过注册初始化了activity,但是其他方式进入的时候的初始化问题
    public static void setup(EMHybridActivity activity) {
        instance.activity = activity;
    }

    // 红点服务的注册,如果未传入回调方法,则默认采用onReceiveReddot方法,即js必须有onReceiveReddot的function
    public void register(WebView webView) {
        register(webView, null);
    }

    // 红点服务的注册,callback
    public void register(WebView webView, final String callback) {
        EMHybridWebView view = (EMHybridWebView) webView;
        activity = (EMHybridActivity) view.getActivity();
        view.setReddotRegister(true);
        if(!TextUtils.isEmpty(callback)) {
            view.setReddotCallback(callback);
        }
    }

    // 红点服务的注销
    public void unRegister(WebView webView) {
        if(webView != null) {
            ((EMHybridWebView)webView).setReddotRegister(false);
        }
    }

    /**
     * 红点计数统计
     *
     * @param data JSON格式数据
     *  示例:data = {templateid:1, pageid:'reddot.html', itemid:'delivery', status:'unread'};
     * @throws Exception
     */
    public void reddotCounting(String data) throws Exception {
        Log.d(TAG,"message:" + data);
        if(activity != null) {
            ReddotMessageManager.getInstance(activity).dispatchReddotMessage(data);
            updateReddot();
        }
    }

    /**
     * 更新界面元素的红点
     */
    public void updateReddot() {
        List list = EMHybridActivity.getWebViewList();
        for(int i = list.size() - 1 ; i >= 0 ; i--) {
            final EMHybridWebView view = (EMHybridWebView)list.get(i);
            // 注册红点服务的界面才执行红点更新
            if(view.isReddotRegister()) {
                String callback = view.getReddotCallback();
                // 如果注册时,未传入回调方法,则默认采用onReceiveReddot方法,即js必须有onReceiveReddot的function
                if(TextUtils.isEmpty(callback)){
                    callback = DEFAULT_JS_CALLBACK;
                }
                updatePageReddot(view, callback);
            }
        }
    }

    /**
     * 更新界面元素的红点
     *
     * @param callbackFunc js中传入的回调方法名
     */
    public void updatePageReddot(final WebView webView, final String callbackFunc) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                String result = "";
                webView.loadUrl("javascript:" + callbackFunc + "('" + result + "')");
            }
        });
    }

}
