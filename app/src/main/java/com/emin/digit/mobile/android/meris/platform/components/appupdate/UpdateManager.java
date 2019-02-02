package com.emin.digit.mobile.android.meris.platform.components.appupdate;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.emin.digit.mobile.android.meris.framework.database.DatabaseManager;
import com.emin.digit.mobile.android.meris.platform.core.EMHybridActivity;
import com.emin.digit.mobile.android.meris.platform.utils.DownloadUtil;
import com.emin.digit.mobile.android.meris.platform.utils.ZipUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Created by Samson on 2016/10/17.
 *
 * 更新管理器,负责app的更新,模版资源的更新
 */
public class UpdateManager {

    private static final String TAG = UpdateManager.class.getSimpleName();

    private EMHybridActivity activity;

    private static UpdateManager instance = new UpdateManager();

    private UpdateManager(){
    }

    public static UpdateManager getInstance(){
        return instance;
    }

    // TODO: 2016/10/18 这种方式有待改善,初始化为了关联主activity,但是跟获取单例分离，看起来很怪
    // TODO: 2016/10/18 下载的资源储存的路劲规则问题
    /**
     * 在主activity创建的时候,初始化UpdateManager,关联activity
     *
     * @param activity 主activity
     */
    public static void setup(EMHybridActivity activity){
        instance.activity = activity;
    }

    /**
     * App的整包更新
     *
     * @param httpUrl
     */
    public void updateApk(final String httpUrl){
        // 不阻塞UI线程,启动子线程执行下载
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = "/sdcard/meris";
                final File apkFile = DownloadUtil.downLoadFile(httpUrl,path);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG,"installing apk file:" + Thread.currentThread().getId());
                        // 打开APK文件,执行安装
                        openFile(apkFile);
                    }
                });
            }
        }).start();
    }

    /**
     * 模版更新
     *
     * @param httpUrl
     */
    public void updateTemplate(final String httpUrl){
        // 不阻塞UI线程,启动子线程执行下载
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = "/sdcard/meris/template/";
                //zipFile = downLoadFile(httpUrl,path);
                final File zipFile = DownloadUtil.downLoadFile(httpUrl,path);
                unzipFile(zipFile, path);
            }
        }).start();
    }

    /**
     * 数据库更新
     *
     * @param updateTableJson
     * @return
     */
    // TODO: 2016/10/26 表不存在的时候,创建表
    public boolean updateDatabase(String updateTableJson){
        try {
            JSONObject tablesObj = new JSONObject(updateTableJson);
            DatabaseManager.getInstance(activity).updateTable(tablesObj);
        }catch (Exception e){
            Log.d(TAG,"Exception occurred");
            return false;
        }
        return true;
    }

    // 打开APK程序,安装
    private void openFile(File apkFile) {
        if(!apkFile.exists()){
            return;
        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        activity.startActivity(intent);
    }

    // 解压
    private int unzipFile(File zipFile, String destPath){
        int status = -1;
        if(zipFile == null){
            return status;
        }
        try {
            status = ZipUtil.upZipFile(zipFile,destPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return status;
    }
}
