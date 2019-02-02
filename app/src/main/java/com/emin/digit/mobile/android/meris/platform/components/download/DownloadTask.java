package com.emin.digit.mobile.android.meris.platform.components.download;

/**
 * author: Samson
 * created on: 2017/11/7 下午 2:30
 * description:
 * 下载任务
 * 1.基于OkHttp
 */
public class DownloadTask {
    private String url;
    private boolean isStop;
    private boolean isCancel;

    public DownloadTask() {

    }

    public boolean start() {
        return true;
    }

    public boolean pause() {
        return true;
    }

    public boolean cancel() {
        return true;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }

    public boolean isCancel() {
        return isCancel;
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }
}
