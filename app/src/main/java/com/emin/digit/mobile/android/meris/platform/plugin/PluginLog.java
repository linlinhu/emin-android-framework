package com.emin.digit.mobile.android.meris.platform.plugin;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.emin.digit.mobile.android.meris.platform.core.PluginParams;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * author: Samson
 * created on: 2017/12/6 下午 3:27
 * description:
 * 日志插件
 */
public class PluginLog {

    private static final String TAG = PluginLog.class.getSimpleName();

    /**
     * 前端写入日志
     * 备注:具体写入日志文件不需要前端关注,具体规则统一由插件来管理
     * 1.日志文件名为日期.log 即yyyyMMdd.log 如:20170824.log
     * 2.内容前追加具体的时间精确到秒,格式为yyyy-MM-dd HH:mm:ss
     * 3.采用UTF-8编码,以便维护查看时避免中文乱码问题
     *
     * @param params 插件对象,其封装了要写入日志的内容
     */
    public void writeLog(PluginParams params) {
        String content = params.getArguments()[0];
        if(TextUtils.isEmpty(content)) {
            return;
        }
        writeLog(content);
    }

    public synchronized static void writeLog(String content) {
        if(TextUtils.isEmpty(content)) {
            return;
        }

        // 目标日志所在文件夹
        // /sdcard/Android/data/com.emin.mobile.android.egobus.driver/apps/egobus-driver/doc
        String folder = "/data/data/com.emin.digit.mobile.android.meris/files";
        File fileDir = new File(folder);
        if (!fileDir.exists()) {
            if (!fileDir.mkdirs()) {
                return;
            }
        }

        Date date = new Date();
        // log文件名字,以日期为基准,不同的日期输出到不同的日期日志文件
        String logFileName = new SimpleDateFormat("yyyyMMdd").format(date) + ".log";
        String filePath = folder + File.separator + logFileName;

        // 日志内容前追加具体的时间点
        String dateTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        String log = dateTimeStr + ":" + content;
        writeToFile(filePath, log, true, true);
    }

    public synchronized static void writeToFile(final String filePath,
                                                final String content,
                                                final boolean append,
                                                final boolean autoLine) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file;
                RandomAccessFile raf = null;
                FileOutputStream out = null;
                try {
                    file = new File(filePath);
                    //判断文件是否存在,不存在则新建
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    if (append) {
                        //如果为追加则在原来的基础上继续写文件
                        raf = new RandomAccessFile(file, "rw");
                        raf.seek(file.length());
                        //raf.writeBytes(content);  // 中文乱码
                        raf.write(content.getBytes("UTF-8"));
                        if (autoLine) {
                            raf.write("\r\n".getBytes());
                        }
                    } else {
                        //重写文件，覆盖掉原来的数据
                        out = new FileOutputStream(file);
                        out.write(content.getBytes());
                        out.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (raf != null) {
                            raf.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public synchronized static void writeFileToSDCard(@NonNull final byte[] buffer,
                                                      @Nullable final String folder,
                                                      @Nullable final String fileName,
                                                      final boolean append,
                                                      final boolean autoLine) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean sdCardExist = Environment.getExternalStorageState().equals(
                        android.os.Environment.MEDIA_MOUNTED);
                String folderPath = "";
                if (sdCardExist) {
                    if (TextUtils.isEmpty(folder)) {
                        //如果folder为空，则直接保存在sd卡的根目录
                        folderPath = Environment.getExternalStorageDirectory() + File.separator;
                    } else {
                        folderPath = Environment.getExternalStorageDirectory()
                                + File.separator + folder + File.separator;
                    }
                } else {
                    return;
                }

                File fileDir = new File(folderPath);
                if (!fileDir.exists()) {
                    if (!fileDir.mkdirs()) {
                        return;
                    }
                }
                File file;
                //判断文件名是否为空
                if (TextUtils.isEmpty(fileName)) {
                    file = new File(folderPath + "app_log.txt");
                } else {
                    file = new File(folderPath + fileName);
                }
                RandomAccessFile raf = null;
                FileOutputStream out = null;
                try {
                    if (append) {
                        //如果为追加则在原来的基础上继续写文件
                        raf = new RandomAccessFile(file, "rw");
                        raf.seek(file.length());
                        raf.write(buffer);
                        if (autoLine) {
                            raf.write("\n".getBytes());
                        }
                    } else {
                        //重写文件，覆盖掉原来的数据
                        out = new FileOutputStream(file);
                        out.write(buffer);
                        out.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (raf != null) {
                            raf.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
