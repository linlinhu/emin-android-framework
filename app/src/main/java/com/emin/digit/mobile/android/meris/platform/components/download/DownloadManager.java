package com.emin.digit.mobile.android.meris.platform.components.download;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * author: Samson
 * created on: 2017/11/7 下午 2:14
 * description:
 * 下载管理器
 * 负责管理下载任务.创建、暂停、取消
 */
public class DownloadManager {
    private static final String TAG = DownloadManager.class.getSimpleName();

    private static volatile DownloadManager instance = null;
    private static Map<String, DownloadTask> mDownloadTasks = null;

    private DownloadManager() {
        mDownloadTasks = new HashMap<>();
    }
    public static DownloadManager getInstance() {
        if(instance == null) {
            synchronized (DownloadManager.class){
                if(instance == null) {
                    instance = new DownloadManager();
                }
            }
        }
        return instance;
    }

    public static Map<String, DownloadTask> getDownloadTasks() {
        return mDownloadTasks;
    }

    public int getTaskCount() {
        Set set = mDownloadTasks.keySet();
        return set.size();
    }

    public void createTask() {

    }

    public void startTask() {

    }

    private void startTask(DownloadTask task) {

    }
}
