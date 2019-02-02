package com.emin.digit.mobile.android.meris.platform.plugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;
import android.webkit.WebView;

import com.emin.digit.mobile.android.meris.framework.zxing.BarcodeController;
import com.emin.digit.mobile.android.meris.platform.core.EMHybridActivity;
import com.emin.digit.mobile.android.meris.platform.core.EMHybridWebView;
import com.emin.digit.mobile.android.meris.platform.core.PluginParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * author: Samson
 * created on: 2016/8/15
 * modified on 2017/12/06 追加api 3、4、5
 *
 * description:
 * 扫码插件api
 * 1.加载二维码扫描控件
 * 2.停止加载二维码扫描控件
 * 3.停止扫描
 * 4.恢复扫描
 * 5.暂停N秒钟后继自动续扫描
 */
public class PluginBarcode {

    private static final String TAG = PluginBarcode.class.getSimpleName();
    final static int REQUEST_CODE_SCAN = 0x0000;

    /**
     * 加载二维码控件
     *
     * @param params 插件参数对象
     * @throws JSONException
     */
    public void loadBarcodeView(PluginParams params) throws JSONException {
        final EMHybridWebView webView = (EMHybridWebView) params.getWebView();
        final EMHybridActivity activity = (EMHybridActivity)webView.getActivity();
//        if(!PermissionUtil.checkPermission(null,activity,Manifest.permission.CAMERA,"",0)) {
//            Log.d(TAG,"==== permission.CAMERA is not permitted");
//            return;
//        }
        JSONObject position = new JSONObject(params.getArguments()[0]);
        Log.d(TAG,"==== position:" + position.toString());
        final int leftOffset = position.optInt("left");     // 0
        final int topOffset = position.optInt("top");       // 200
        final int rightOffset = position.optInt("right");   // 720
        final int bottomOffset = position.optInt("bottom"); // 682
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Rect rect = new Rect(leftOffset,topOffset,rightOffset,bottomOffset);
                BarcodeController.getInstance().loadBarcodeView(activity,rect);
            }
        });
    }

    /**
     * 结束加载二维码控件
     *
     * @param params 插件参数对象
     */
    public void stopBarcode(PluginParams params){
        final EMHybridWebView webView = (EMHybridWebView) params.getWebView();
        final EMHybridActivity activity = (EMHybridActivity)webView.getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"== will stop BarcodeView");
                BarcodeController.getInstance().cancel();
            }
        });
    }

    /**
     * 停止扫描
     * 备注:扫码控件还在
     *
     * @param params 插件参数对象
     */
    public void stopScan(PluginParams params) {
        final EMHybridWebView webView = (EMHybridWebView) params.getWebView();
        final EMHybridActivity activity = (EMHybridActivity)webView.getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BarcodeController.getInstance().stopScan();
            }
        });
    }

    /**
     * 恢复/重新扫描
     *
     * @param params 插件参数对象
     */
    public void resumeScan(PluginParams params) {
        final EMHybridWebView webView = (EMHybridWebView) params.getWebView();
        final EMHybridActivity activity = (EMHybridActivity)webView.getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BarcodeController.getInstance().continueScan();
            }
        });
    }

    /**
     * 暂停N秒钟后自动继续扫描
     *
     * @param params 插件参数对象,其包装了前端传入的秒数
     */
    public void pauseScan(PluginParams params) {
        final long seconds = Long.valueOf(params.getArguments()[0]);
        final EMHybridWebView webView = (EMHybridWebView) params.getWebView();
        final EMHybridActivity activity = (EMHybridActivity)webView.getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long millis = seconds * 1000;
                BarcodeController.getInstance().pauseScan(millis);
            }
        });
    }

    public void setFlashLight(PluginParams params) {
        final String action = params.getArguments()[0];
        boolean isOn = false;
        if(action.equalsIgnoreCase("on")) {
            isOn = true;
        }
        final EMHybridWebView webView = (EMHybridWebView) params.getWebView();
        final EMHybridActivity activity = (EMHybridActivity)webView.getActivity();
        final boolean on = isOn;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "== BarcodeController openFlashLight:" + on);
                BarcodeController.getInstance().openFlashLight(on);
            }
        });
    }

    @Deprecated
    private void startSystemCameraOnly(Context context){
        Intent intentCamera = new Intent();
        String sysCameraAction = "android.media.action.STILL_IMAGE_CAMERA";
        intentCamera.setAction(sysCameraAction);
        context.startActivity(intentCamera);
    }
}
