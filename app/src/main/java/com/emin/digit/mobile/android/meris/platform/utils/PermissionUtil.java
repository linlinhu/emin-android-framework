package com.emin.digit.mobile.android.meris.platform.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * android系统权限工具
 * 1.android系统在6.0以后对各种系统权限(如摄像头等)需要用户主动允许才能使用
 *   6.0以下直接弹出权限框,6.0以后需要手动检查
 *   比如:在华为测试机(4.4)不需要代码检查,都会自动弹出权限是否允许,在小米的6.0.1上,则默认都不询问,
 *   不加入代码主动检查,则需要用户自己到系统设置去允许
 *
 * Created by Samson on 2017/2/13 .
 */
public class PermissionUtil {

    public static boolean checkPermission(Fragment fragment, final Activity activity,
                                          String permission, String hint, int requestCode) {
        //检查权限
        if (ContextCompat.checkSelfPermission(activity, permission) !=  PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                //显示我们自定义的一个窗口引导用户开启权限
                //showPermissionSettingDialog(activity, hint);
            } else {
                //申请权限
                if (fragment == null) {
                    ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
                } else {
                    fragment.requestPermissions(new String[]{permission}, requestCode);
                }
            }
            return false;
        } else {  //已经拥有权限
            return true;
        }
    }

    /**
     * 使用同意摄像头使用权限
     *
     * @return true 同意 false 拒绝
     */
    public static boolean isCameraPermitted(){
        try {
            Camera.open().release();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 用户是否同意录音使用权限
     *
     * @return true 同意 false 拒绝
     */
    public static boolean isVoicePermitted() {

        try {
            AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC, 22050, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, AudioRecord.getMinBufferSize(22050, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT));
            record.startRecording();
            int recordingState = record.getRecordingState();
            if(recordingState == AudioRecord.RECORDSTATE_STOPPED){
                return false;
            }
            record.release();
            return true;
        } catch (Exception e) {
            return false;
        }

    }

}
