package com.emin.digit.mobile.android.meris.platform.utils;

import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Samson on 2016/10/17.
 */
public class DownloadUtil {

    private static final String TAG = DownloadUtil.class.getSimpleName();

    /**
     * 文件下载
     * 
     *
     * @param httpUrl 资源URL
     * @param destPath 下载的资源储存路劲
     * @return 下载完成的资源File对象
     */
    public static File downLoadFile(String httpUrl, String destPath){
        Log.d(TAG,"download file:" + httpUrl);
        File dir = new File(destPath);
        // 在sdcard创建目录,注意在AndroidManifest.xml中配置权限:WRITE_EXTERNAL_STORAGE
        if(!dir.exists()) {
            if (dir.mkdir()) {
                Log.d(TAG, "make dir succeed!");
            }
        }
        // 取Url中的文件名
        String fileName = getFileName(httpUrl);
        String filePath = destPath + "/" + fileName;

        final File file = new File(filePath);
        try {
            URL url = new URL(httpUrl);
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream is = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buf = new byte[256];
                conn.connect();
                double count = 0;
                // TODO: 2016/10/17 下载过程中相应超时的异常处理
                if (conn.getResponseCode() >= 400) {
                    //Toast.makeText(activity, "下载连接超时", Toast.LENGTH_SHORT).show();
                } else {
                    while (count <= 100) {
                        if (is != null) {
                            int numRead = is.read(buf);
                            if (numRead <= 0) {
                                break;
                            } else {
                                fos.write(buf, 0, numRead);
                            }
                        } else {
                            break;
                        }
                    }
                }
                conn.disconnect();
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return file;
    }

    // 从url中截取apk的文件名
    private static String getFileName(String httpUrl){
        String[] f =  httpUrl.split("/");
        String name = f[f.length-1];
        return name;
    }
}
