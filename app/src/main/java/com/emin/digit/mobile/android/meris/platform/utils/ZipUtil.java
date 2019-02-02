package com.emin.digit.mobile.android.meris.platform.utils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Samson on 2016/10/17.
 *
 * Zip工具类,负责解压zip格式的压缩包
 */
public class ZipUtil {


    // TODO: 2016/10/17 对文件的压缩功能

    public void zipFile(String path) {
    }

    public void zipFile(File sourceFile) {
    }


    /**
     * zip文件解压功能
     *
     * @param sourcePath zip压缩包文件的路劲
     * @param destPath 将压缩包解压的目标文件夹径路
     *                 要以"/"结束,如/sdcard/meris/
     * @return 解压的状态,0为成功
     * @throws IOException 输入输出异常
     */
    public int unZipFile(String sourcePath, String destPath) throws IOException{
        File sourceFile = new File(sourcePath);
        int status = upZipFile(sourceFile, destPath);
        return status;
    }

    /**
     * zip文件解压功能
     *
     * @param zipFile zip压缩包文件对象
     * @param folderPath 将压缩包解压的目标文件夹径路
     *                   要以"/"结束,如/sdcard/meris/
     * @return 解压的状态,0为成功
     *
     * @throws IOException 输入输出异常
     */
    public static int upZipFile(File zipFile, String folderPath) throws IOException {
        ZipFile zfile = new ZipFile(zipFile);
        Enumeration zList = zfile.entries();
        ZipEntry ze = null;
        byte[] buf = new byte[1024];
        while (zList.hasMoreElements()) {
            ze = (ZipEntry) zList.nextElement();
            if (ze.isDirectory()) {
                Log.d("upZipFile", "ze.getName() = " + ze.getName());
                String dirstr = folderPath + ze.getName();
                dirstr = new String(dirstr.getBytes("8859_1"), "GB2312");
                Log.d("upZipFile", "str = " + dirstr);
                File f = new File(dirstr);
                f.mkdir();
                continue;
            }
            Log.d("upZipFile", "ze.getName() = " + ze.getName());
            OutputStream os = new BufferedOutputStream(new FileOutputStream(getRealFileName(folderPath, ze.getName())));
            InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
            int readLen = 0;
            while ((readLen = is.read(buf, 0, 1024)) != -1) {
                os.write(buf, 0, readLen);
            }
            is.close();
            os.close();
        }
        zfile.close();
        Log.d("upZipFile", "finish");
        return 0;
    }

    /**
     * 给定根目录，返回一个相对路径所对应的实际文件名.
     *
     * @param baseDir     指定根目录
     * @param absFileName 相对路径名，来自于ZipEntry中的name
     * @return java.io.File 实际的文件
     */
    public static File getRealFileName(String baseDir, String absFileName) {
        String[] dirs = absFileName.split("/");
        String lastDir = baseDir;
        if (dirs.length > 1) {
            for (int i = 0; i < dirs.length - 1; i++) {
                lastDir += (dirs[i] + "/");
                File dir = new File(lastDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                    Log.d("getRealFileName", "create dir = " + (lastDir + "/" + dirs[i]));
                }
            }
            File ret = new File(lastDir, dirs[dirs.length - 1]);
            Log.d("upZipFile", "2ret = " + ret);
            return ret;
        } else {
            return new File(baseDir, absFileName);
        }
    }


}
