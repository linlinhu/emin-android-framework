package com.emin.digit.mobile.android.meris.platform.core;

import android.app.ProgressDialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.emin.digit.mobile.android.meris.R;
import com.emin.digit.mobile.android.meris.framework.zxing.BarcodeController;
import com.emin.digit.mobile.android.meris.platform.components.appupdate.UpdateManager;
import com.emin.digit.mobile.android.meris.platform.components.job.JobManager;
import com.emin.digit.mobile.android.meris.platform.components.net.NetBroadcastReceiver;
import com.emin.digit.mobile.android.meris.platform.components.redot.ReddotManager;
import com.emin.digit.mobile.android.meris.platform.components.timer.TimerManager;
import com.emin.digit.mobile.android.meris.platform.utils.FileUtil;
import com.emin.digit.mobile.android.meris.platform.utils.WebviewUtil;
import com.emin.digit.mobile.android.meris.service.CoreService;
import com.emin.digit.mobile.android.meris.service.ServiceCenter;
import com.emin.digit.mobile.android.meris.platform.core.EMHybridWebView.ViewType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.LinkedList;

/**
 * Created by Samson on 16/8/5.
 * <p/>
 * 以HTML5为前端的混合开发中,原生层总控Activity
 * 负责初始化配置;
 * 负责加载WebView,以及在WebView注入EMBridge对象,以供页面统一调用原生Plugin实现原生android功能;
 * <p/>
 * 变更履历
 * 2016/11/02 template引擎API追加
 * 2017/02/10 webviewList的管理:通过唯一标识获取,简单进出动画封装
 */
public class EMHybridActivity extends EMBaseActivity {

    private static final String TAG = EMHybridActivity.class.getSimpleName();

    // 前端所有web app资源的根路径
    private static final String ENV_WEB_APP_BASE_PATH = "file:///android_asset/apps/";
    // web app的统一配置文件
    private static final String ENV_WEB_APP_CONFIG_FILE = "apps/WebManifest.json";
    // 当前web app的资源路劲.例如:"file:///android_asset/apps/FrameworkTest/www/"
    private String mWebSourcePath;
    // 当前web app的第一个加载界面.例如:init.html
    private String mIndexUrl;
    // 布局UI容器
    private FrameLayout containerView;
    //private FrameLayout rootLayout;
    // 加载HTML页面的WebView
    private EMHybridWebView mWebView;
    // 前端界面webview列表
    private static LinkedList<EMHybridWebView> webViewList = new LinkedList<>();

    private ProgressDialog progressDialog;
    private ServiceCenter serviceCenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupApp();
        setupWebApp();
        setupComponents();
        //setupService();
    }

    // App初始化
    private void setupApp() {
        // 取消默认的标题栏以及全屏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.bridge_activity);
//        rootLayout = (FrameLayout) findViewById(R.id.idActivityHybrid);
        containerView = (FrameLayout) findViewById(R.id.idContainerView);
    }

    /**
     * 前端web app的初始化
     * 资源路劲、首页加载
     */
    private void setupWebApp() {
        String content = FileUtil.readAssetsFile(this, ENV_WEB_APP_CONFIG_FILE);
        try {
            JSONObject obj = new JSONObject(content);
            String appId = obj.optString("id");
            mWebSourcePath = ENV_WEB_APP_BASE_PATH + appId +  File.separator;
            String srcPath = obj.optString("source_path");
            if (!TextUtils.isEmpty(srcPath)) {
                mWebSourcePath += srcPath +  File.separator;
            }
            mIndexUrl = obj.optString("launch_path");
            loadInitView(mIndexUrl);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "==读取web 配置失败:" + e.getMessage());
        }
    }

    /**
     * 基础构件的初始化
     */
    private void setupComponents() {
        // 插件管理器配置
        PluginManager.setup();
        // 红点管理器配置
        ReddotManager.setup(this);
        // 更新管理器配置
        UpdateManager.setup(this);
        // Job管理器配置
        JobManager.setup(this);
        // Timer引擎配置
        TimerManager.setup(this);
    }

    /**
     * service初始化
     */
    private void setupService() {
        // 创建服务中心
        serviceCenter = new ServiceCenter(this);
        // 启动service独立进程
        serviceCenter.startUpService(CoreService.class);
        // 启动推送服务
        // serviceCenter.startupPushService();
        // serviceController.startUpService(PushServiceMqtt.class);
        //this.startService(new Intent(this,EMPushService.class));
        // int pid = android.os.Process.myPid();
    }

    // TODO: 16/8/31 优化:初始化界面不需要加入这个webView type:9
    public void loadInitView(String url) {
        Log.d(TAG, "== load init view:" + url);
        url = getFullPathForFile(url);
        //mWebView = createWebView(url, 9);
        mWebView = createWebView(url, ViewType.INIT);
        mWebView.loadUrl(url);
    }

    /**
     * 加载webview
     *
     * @param options json对象
     *                例如
     *                options = {url:'index.html',
     *                id:'index',
     *                extras:{userName:'sam',company:'emin'}
     *                }
     */
    // TODO: 16/8/19 WebView type的设计,启动页面,广告页面,引导页面等,因为像启动页面显示完了不需要后续管理
    public void loadContentWebview(JSONObject options) {
        String url = options.optString("url");
        String id = options.optString("id");
        url = getFullPathForFile(url);
        //EMHybridWebView webView = createWebView(url, 11);
        EMHybridWebView webView = createWebView(url, ViewType.CONTENT);
        webView.setViewId(id);
        if (options.has("extras")) {
            JSONObject extras = options.optJSONObject("extras");
            webView.setExtras(extras);
        }
        mWebView = webView;
        mWebView.loadUrl(url);
        Log.d(TAG, "### load ContentWebview:" + mWebView + " mWebView.viewId:" + mWebView.getViewId());

//        showProgressDialog();
    }

    /**
     * 创建WebView
     *
     * @param url 页面url
     * @return EMHybridWebView
     */
    public EMHybridWebView createWebView(String url, ViewType type) {
        EMHybridWebView webView = new EMHybridWebView(this, EMHybridActivity.this, url);
        webView.setViewType(type);
        webView.setUuid(WebviewUtil.genWebViewId());
        return webView;
    }

    public void preloadWebViews(String[] urlArray) {
    }

    // web前端页面跳转传的是相对路劲,必须拼接成android 资源目录下的全路劲
    private String getFullPathForFile(String url) {
        return mWebSourcePath + url;
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        Window wd = progressDialog.getWindow();
        WindowManager.LayoutParams lp = wd.getAttributes();
        lp.alpha = 0.8f;
        wd.setAttributes(lp);
        wd.setGravity(Gravity.BOTTOM);
        progressDialog.show();
    }

    // - - - - - - - - - - Activity的生命周期各个阶段涉及的WebView处理
    // Activity醒来,webView也开始工作
    @Override
    protected void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.onResume();
        }
        Rect barcodeRect = BarcodeController.getInstance().getBarcodeRect();
        if (BarcodeController.getInstance().isPause() && barcodeRect != null) {
            BarcodeController.getInstance().loadBarcodeView(this, barcodeRect);
            BarcodeController.getInstance().setPause(false);
        }
//      SharedPreferences p = getSharedPreferences(PushServiceMqtt.TAG, MODE_PRIVATE);
//      boolean started = p.getBoolean(PushServiceMqtt.PREF_STARTED, false);
    }

    // Activity暂停,webView暂停
    @Override
    protected void onPause() {
        super.onPause();
        if (mWebView != null) {
            mWebView.onPause();
        }
        if (BarcodeController.getInstance().isLoaded()) {
            BarcodeController.getInstance().setPause(true);
            BarcodeController.getInstance().stop();
        }
    }

    // Activity停止,webView停止加载
    @Override
    protected void onStop() {
        super.onStop();
        if (mWebView != null) {
            mWebView.stopLoading();
        }
    }

    // Activity销毁,webView销毁
    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyAllWebViews();
        if (serviceCenter != null)
            serviceCenter.stopBroadcastListen();
    }

    // 销毁所有的webView
    private void destroyAllWebViews() {
        for (WebView webView : webViewList) {
            webView.destroy();
        }
    }

    private void destroyWebView(WebView view) {
        view.destroy();
        //NetBroadcastReceiver.removeListener((EMHybridWebView)view);
    }

    /**
     * 监听设备的返回按键事件
     * 由于是系统的事件,对界面的控制会有不可控的影响,需要将该事件做转发处理,以便在界面有处理的机会
     *
     * @param keyCode 按键代码
     * @param event   事件
     * @return boolean
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //shouldToLastWebView();
            mWebView.loadUrl("javascript:eminBack()");
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public void shouldToLastWebView() {
        // 当在界面有back方法时候,调用back方法,因为点击设备的返回键,界面没办法控制
        //WebviewUtil.execCallback(mWebView, "eminBack", null);

        // 插件二维码View加载方式的状态,如果有就停止加载
        BarcodeController.getInstance().cancel();
        if (webViewList.size() <= 1) {
            exitAppWhenDoublePressed();
        } else {
            popLastWebview();
        }
    }

    public void willToLastWebview() {
        // 当在界面有back方法时候,调用back方法,因为点击设备的返回键,界面没办法控制
        //WebviewUtil.execCallback(mWebView, "eminBack", null);

        // 插件二维码View加载方式的状态,如果有就停止加载
        BarcodeController.getInstance().cancel();
        popLastWebview();
    }

    private long firstTimePressed = 0;
    private void exitAppWhenDoublePressed() {
        // 2秒之内按两次,则退出应用
        long nextTimePressed = System.currentTimeMillis();
        if ((nextTimePressed - firstTimePressed) > 2 * 1000) {
            Toast.makeText(this, "再按一次退出应用", Toast.LENGTH_SHORT).show();
            firstTimePressed = nextTimePressed;
        } else {
            finish();
            System.exit(0);
        }
    }

    /**
     * 加载webview到最顶端
     * 往左切入动画
     */
    public void pushWebview() {
        Log.d(TAG, "!!!! webview size:" + webViewList.size());
        if (mWebView.getParent() != null) {
            Log.d(TAG, "### has parent:" + mWebView + " it'parent:" + mWebView.getParent() + " mWebView.viewId:" + mWebView.getViewId());
            Log.d(TAG, "### containerView:" + containerView);
            //((FrameLayout)mWebView.getParent()).removeView(mWebView);
        }
        Log.d(TAG, "### push Webview, mWebView.viewId:" + mWebView.getViewId());
        containerView.addView(mWebView);
        int animId = R.anim.transition_push_in;
        if(webViewList.size() == 1) {
            // 解决初始化界面有一个白屏闪一下的问题
            animId = R.anim.transition_push_in_init;
        }
        Animation translate_in = AnimationUtils.loadAnimation(mWebView.getContext(), animId);
        translate_in.setFillAfter(true);
        translate_in.setDetachWallpaper(true);
        mWebView.setAnimation(translate_in);
    }

    public void pushWebview(WebView webView) {
        containerView.addView(webView);
        Animation translate_in = AnimationUtils.loadAnimation(webView.getContext(), R.anim.transition_push_in);
        translate_in.setFillAfter(true);
        translate_in.setDetachWallpaper(true);
        webView.setAnimation(translate_in);
    }

    /**
     * 移除最后加载界面WebView
     * 动画:向右弹出
     */
    public void popLastWebview() {
        Log.d(TAG, "### pop LastWebview,mWebView:" + mWebView + " mWebView.viewId:" + mWebView.getViewId() + " webViewList count:" + webViewList.size());
        containerView.removeView(mWebView);
        Animation translate_out = AnimationUtils.loadAnimation(mWebView.getContext(), R.anim.tansition_pop_out);
        translate_out.setFillAfter(true);
        translate_out.setDetachWallpaper(true);
        mWebView.setAnimation(translate_out);

        webViewList.removeLast();
        NetBroadcastReceiver.removeListener(mWebView);
        //destroyWebView(mWebView); // Add on 2017/01/21 remove还是不够,还需要销毁该webview NG,影响动画加载
        mWebView = webViewList.getLast();
        Log.d(TAG, "### after pop LastWebview,mWebView:" + mWebView + " mWebView.viewId:" + mWebView.getViewId() + " webViewList count:" + webViewList.size());
    }

    /**
     * 移除目标index的webview之后的webview,
     * 即界面上显示的是index所对应的webview,之后加载的都移除,并保留移除的动画
     *
     * @param index 目标webview的索引
     */
    public void popWebviewFromIndex(int index) {
        for (int i = webViewList.size() - 1; i >= 0; i--) {
            if (i <= index) {
                break;
            }
            popLastWebview();
        }
    }

    /**
     * webView加载、移除handler
     * 通过监听WebViewClient的加载情况,完成后显示在窗口显示webView,避免白屏情况
     */
    public Handler webViewHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int type = msg.what;
            if(type == ViewType.CONTENT.ordinal()) {
                Log.d(TAG, "== ViewType.CONTENT:" + mWebView);
                webViewList.add(mWebView);
                try {
                    pushWebview();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                // added 2017/11/30 前端界面加载完之后执行eminReady()方法
                // 解决前端某些事件必须要在界面加载完之后才触发的事件,如模板渲染需要获取到数据
                mWebView.loadUrl("javascript:eminReady()");
            } else {
                Log.d(TAG, "== ViewType.INIT/AD view,will not add into container view");
                mWebView.loadUrl("javascript:eminReady()");
            }
            return false;
        }
    });

    public static LinkedList<EMHybridWebView> getWebViewList() {
        return webViewList;
    }

    /**
     * 通过webview唯一性标识获取webview的index
     *
     * @param viewId 标识
     * @return 匹配的webview的索引
     */
    public int getWebviewIndexById(String viewId) {
        int index = -1;
        if (TextUtils.isEmpty(viewId)) return index;
        for (int i = 0; i < webViewList.size(); i++) {
            String identifier = webViewList.get(i).getViewId();
            if (identifier.equals(viewId)) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 通过唯一性id获取加载的webview
     *
     * @param id 标识
     * @return 匹配的webview
     */
    public EMHybridWebView getWebviewById(String id) {
        int index = getWebviewIndexById(id);
        if (index == -1) {
            return null;
        }
        return webViewList.get(index);
    }

    public String getIndexUrl() {
        return mIndexUrl;
    }

    public void setIndexUrl(String mIndexUrl) {
        this.mIndexUrl = mIndexUrl;
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public FrameLayout getContainerView() {
        return containerView;
    }

    private void showInfo() {
        Log.d(TAG, "== EMHybridActivity:" + this);
        Log.d(TAG, "== EMHybridActivity.this:" + EMHybridActivity.this);
        Log.d(TAG, "== webView in Activity initialize:" + mWebView);
        Log.d(TAG, "== WebView getContext:" + mWebView.getContext());
    }
}
