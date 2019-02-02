package com.emin.digit.mobile.android.meris.platform.core;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.emin.digit.mobile.android.meris.R;
import com.emin.digit.mobile.android.meris.platform.components.net.NetBroadcastReceiver;
import com.emin.digit.mobile.android.meris.platform.utils.JSUtil;
import com.emin.digit.mobile.android.meris.platform.utils.WebviewUtil;

import org.json.JSONObject;

/**
 * 混合开发WebView封装
 *
 * Created by Samson on 2016/8/5.
 * Modified on 17/01/05 更新WebView的配置使Web前端能正常使用H5的localStorage
 * Modified on 17/01/11 追加webview 的viewId,传参等供用户跳转时配置
 * Modified on 17/01/22 追加网络监听配置
 * Modified on 17/01/22 追加webview系统唯一性标识,该标识区别于viewId,后者是用户配置的,可重复
 */
public class EMHybridWebView extends WebView implements NetBroadcastReceiver.NetEventHandler{

    private static final String TAG = EMHybridWebView.class.getSimpleName();
    // 上下文
    private Context mContext;
    // 关联的Activity
    private Activity mActivity;
    // WebViewClient
    private EMHybridWebViewClient mWebViewClient;
    // WebChromeClient
    private EMHybridWebChromeClient mWebViewChromeClient;
    // 加载的资源地址
    private String mUrl;
    // 用户配置的webview的标识(可重复)
    private String viewId;
    // 系统生成的唯一标识
    private String uuid;
    // 界面(webview)传递参数对象
    private JSONObject extras;
    // WebView注入的对象暴露给javascript的名称
    private static final String INJECTED_BRIDGE_NAME = "EminBridge";
    // 网页加载的进度条
    private ProgressBar mProgressBar;
    // view类型,区分某些view不加入管理列表
    private ViewType viewType;

    public enum ViewType {
        INIT,
        CONTENT,
        AD
    }
    // - - - - - - - - - 界面相关 - - - - - - - - -
    private FrameLayout mLayout;
    // root view
    private FrameLayout mBrowserFrameLayout;
    // 内容显示区域
    private FrameLayout mContentView;

    // - - - - - - - - - 界面是否注册红点服务 - - - - - - - - -
    private boolean isReddotRegister = false;
    // 界面的红点服务回调方法名
    private String reddotCallback;

    // 界面网络监听相关
    // 界面是否注册了网络监听服务
    private boolean isNetStateRegister = false;
    // 界面的网络状态变更回调方法名
    private String netStateCallback;

    // - - - - - - - - - - - 构造方法 - - - - - - - - - - -
    public EMHybridWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EMHybridWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public EMHybridWebView(Context context, Activity activity){
        super(context);
        mActivity = activity;
        init(context);
    }

    public EMHybridWebView(Context context, Activity activity, String url){
        super(context);
        mActivity = activity;
        mUrl = url;
        init(context);
    }

    // 私有初始化方法,内部进行WebView各种配置
    private void init(Context context){
        mContext = context;
        // 布局的设置
        //settingLayout(context);
        // 设置WebViewClient
        mWebViewClient = new EMHybridWebViewClient();
        this.setWebViewClient(mWebViewClient);
        // 设置WebChromeClient
        mWebViewChromeClient = new EMHybridWebChromeClient();
        this.setWebChromeClient(mWebViewChromeClient);
        // WebView配置
        settingWebView();
        // 注入对象配置-js层的调用接口
        configJavascriptInterface();

        // 加载进度条配置
        //settingProgressBar();
    }

    // 废弃
    @Deprecated
    private void settingLayout(Context context){
        mLayout = new FrameLayout(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        mBrowserFrameLayout = (FrameLayout)inflater.inflate(R.layout.bridge_webview,null);
        mContentView = (FrameLayout)mBrowserFrameLayout.findViewById(R.id.main_content);
        final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        mLayout.addView(mBrowserFrameLayout,COVER_SCREEN_PARAMS);
        mContentView.addView(this);
    }

    // WebView配置
    @SuppressWarnings("setJavaScriptEnabled")
    private void settingWebView() {
        // 允许通过chrome://inspect 进行调试
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            WebView.setWebContentsDebuggingEnabled(true);
        }
        this.setHorizontalScrollBarEnabled(false); //水平方向滚动条禁用
        this.setHorizontalScrollBarEnabled(false); //垂直方向滚动条禁用
        this.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        WebSettings settings = this.getSettings();
        settings.setJavaScriptEnabled(true); // 支持Javascript
        String encodingName = "UTF-8";
        settings.setDefaultTextEncodingName(encodingName); // 配置字符编码
        settings.setSupportZoom(false); // 禁用缩放

        // modified 0105
        settings.setAllowFileAccess(true);
        String ua = settings.getUserAgentString();
        settings.setUserAgentString(ua);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowUniversalAccessFromFileURLs(true); // winnie-Framework7 page louter

        // 屏蔽长按事件。默认情况下长按,会出现全选,复制等小功能
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG,"== view onLongClick :" + v);
                return true;
            }
        });
    }

    // 注入EminBridge对象
    private void configJavascriptInterface(){
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)) {
            Log.i(TAG, "Disabled addJavascriptInterface() bridge since Android version is old.");
            return;
        }
        // 注入的对象
        EMBridge injectedObject = new EMBridge(mActivity, this);
        // javascript通过该名字调用注入对象的方法
        String nameUsedInJs = INJECTED_BRIDGE_NAME;
        this.addJavascriptInterface(injectedObject, nameUsedInJs);
    }

    // 网页加载进度条配置
    private void settingProgressBar(){
        mProgressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleHorizontal);
        mProgressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 3, 0, 0));
        this.addView(mProgressBar);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG,"== onKeyDown keyCode:" + keyCode + "KeyEvent:" + event);
        return super.onKeyDown(keyCode, event);
    }

    // ===== NetEventHandler Interface
    @Override
    public void onNetStateChange(int netType){
        Log.d(TAG,"== onNetChange net type:" + netType);
        String param = JSUtil.wrapKeyValue("type", Integer.valueOf(netType));
        WebviewUtil.execCallback(this,netStateCallback, param);
    }

    // ====================== Setter and Getter ==========================
    public FrameLayout getLayout(){
        return mLayout;
    }

    public Activity getActivity() {
        return mActivity;
    }

    public ViewType getViewType() {
        return viewType;
    }

    public void setViewType(ViewType viewType) {
        this.viewType = viewType;
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public JSONObject getExtras() {
        return extras;
    }

    public void setExtras(JSONObject extras) {
        this.extras = extras;
    }

    // 红点服务注册标志的获取
    public boolean isReddotRegister() {
        return isReddotRegister;
    }

    // 红点服务注册标志的设置
    public void setReddotRegister(boolean reddotRegister) {
        isReddotRegister = reddotRegister;
    }

    public String getReddotCallback() {
        return reddotCallback;
    }

    public void setReddotCallback(String reddotCallback) {
        this.reddotCallback = reddotCallback;
    }

    public boolean isNetStateRegister() {
        return isNetStateRegister;
    }

    public void setNetStateRegister(boolean netStateRegister) {
        isNetStateRegister = netStateRegister;
    }

    public String getNetStateCallback() {
        return netStateCallback;
    }

    public void setNetStateCallback(String netStateCallback) {
        this.netStateCallback = netStateCallback;
    }
}
