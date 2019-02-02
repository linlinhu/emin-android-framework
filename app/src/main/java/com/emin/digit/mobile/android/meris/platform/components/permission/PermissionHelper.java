package com.emin.digit.mobile.android.meris.platform.components.permission;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * author: Samson
 * created on: 2017/10/17 上午 11:23
 * description:
 * 前端操作数据库插件
 * 关于Android M运行时权限
 * http://www.jianshu.com/p/d3a998ec04ad
 */
public class PermissionHelper {
    private static final String TAG = PermissionHelper.class.getSimpleName();

    private Context mContext = null;

    // 单例
    private static PermissionHelper instance = null;
    public static PermissionHelper getInstance(Context context) {
        if(instance == null) {
            synchronized (PermissionHelper.class) {
                if(instance == null) {
                    // 通过getApplicationContext()保证context的全局与统一,
                    // 而不必关心源context(如一个Activity的生命周期、内存泄露等问题)
                    instance = new PermissionHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private PermissionHelper(Context context) {
        mContext = context;
    }

    /**
     * 批量验证权限
     *
     * @param perNameArray 目标权限名称数组,
     *                     如["GPS","SMS"]
     * @return 验证结果JSON对象,
     *         如{"GPS":true,"SMS":false}
     * @throws JSONException
     */
    public JSONObject checkForBatch(String[] perNameArray) throws JSONException {
        if(perNameArray == null || perNameArray.length == 0) {
            return null;
        }
        JSONObject result = new JSONObject();
        for (String perName : perNameArray) {
            boolean isPermit = check(perName);
            result.put(perName,isPermit);
        }
        return result;
    }

    /**
     * 从权限的配置文件中读取目标权限,并验证每一项权限的情况
     *
     * @param filePath 权限配置文件的路劲
     * @return 验证结果JSON对象,
     *         如{"GPS":true,"SMS":false}
     */
    public JSONObject checkForPropertyFile(String filePath) throws JSONException{
        if(TextUtils.isEmpty(filePath)) {
            return null;
        }
        PermissionLoader loader = PermissionLoader.getInstance(mContext);
        String content = loader.load(filePath);
        String log = String.format("file:%s\ncontent:%s",filePath,content);
        Log.i(TAG, log);

        JSONObject perObj = new JSONObject(content).optJSONObject("permissions");
        JSONObject result = new JSONObject();

        Iterator<String> it = perObj.keys();
        while (it.hasNext()) {
            String perName = it.next();
            boolean isPermit = check(perName);
            result.put(perName,isPermit);
        }
        Log.i(TAG,result.toString());
        return result;
    }

    public boolean check(String permissionName) {
        boolean isPermit = false;
        switch (permissionName) {
            case "GPS":
                isPermit = checkLocation();
                break;
            case "Camera":
                isPermit = checkCameraUsable();//checkCamera();
                break;
            case "Galley":
                isPermit = checkGalley();
                break;
            case "Storage":
                isPermit = checkStorage();
                break;
            case "Phone":
                isPermit = checkPhone();
                break;
            case "SMS":
                isPermit = checkSMS();
                break;
            case "Notification":
                isPermit = checkNotification();
                break;
            default:
                break;
        }
        return isPermit;
    }

    public boolean checkLocation() {
        String name = Manifest.permission.LOCATION_HARDWARE;
        int result = ContextCompat.checkSelfPermission(mContext, name);
        Log.d(TAG, "###checkLocation PackageManager.PERMISSION_GRANTED:" + PackageManager.PERMISSION_GRANTED);
        Log.d(TAG, "###checkLocation result:" + result);
        int checkRel = android.support.v4.content.PermissionChecker.checkSelfPermission(mContext, name);
        Log.d(TAG, "###checkLocation checkRel:" + checkRel);
        return checkRel == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkCamera() {
//        int perm = ctx.checkCallingOrSelfPermission("android.permission.CAMERA");
//        return perm == PackageManager.PERMISSION_GRANTED;
        // For Android < Android M, self permissions are always granted.

        // API 23 中新增 :检查权限checkSelfPermission(),申请权限requestPermissions()
        String name = Manifest.permission.CAMERA;
        int result = ContextCompat.checkSelfPermission(mContext, name);
//        int checkRel = android.support.v4.content.PermissionChecker.checkSelfPermission(mContext, name);
        Log.d(TAG, "== check Camera permission result:" + result);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    // 采用调用摄像头的方式,如果不能正常调用,表示无权限,但是也有可能是摄像头故障的原因而不是权限问题
    public boolean checkCameraUsable() {
        boolean canUse = true;
        Camera mCamera =null;
        try{
            mCamera = Camera.open();
            // setParameters 是针对魅族MX5。MX5通过Camera.open()拿到的Camera对象不为null
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        }catch(Exception e) {
            canUse = false;
        }
        if(mCamera != null) {
            mCamera.release();
        }
        return canUse;
    }

    public boolean checkGalley() {
        String name = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int result = ContextCompat.checkSelfPermission(mContext, name);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkStorage() {
        String name = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int perm = mContext.checkCallingOrSelfPermission(name);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkPhone() {
        String name = Manifest.permission.CALL_PHONE;
        int result = ContextCompat.checkSelfPermission(mContext, name);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkSMS() {
        String name = Manifest.permission.READ_SMS;
        int result = ContextCompat.checkSelfPermission(mContext, name);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkNotification() {
        String name = Manifest.permission.ACCESS_NOTIFICATION_POLICY;
        int result = ContextCompat.checkSelfPermission(mContext, name);
        return result == PackageManager.PERMISSION_GRANTED;
    }

}
