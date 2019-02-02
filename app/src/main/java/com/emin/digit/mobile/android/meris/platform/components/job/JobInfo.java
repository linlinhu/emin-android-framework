package com.emin.digit.mobile.android.meris.platform.components.job;

import android.util.Log;

import java.util.Date;

/**
 * Created by Samson on 2016/10/27.
 */
public class JobInfo {

    private static final String TAG = JobInfo.class.getSimpleName();

    private int id;                // id
    private String name;           // 可显示的名称
    private long millisDelay;      // 延迟几毫秒
    private Date executeTime;      // 指定时间执行
    private Date startDate;        // 开始时间
    private Date endDate;          // 截止时间
    private String repeatType;     // 重复次数
    private long repeatInterval;   // 多少时间重复一次
    private boolean isCancel;      // 是否有效的

    private static int generateId = 1;

    public JobInfo(){
        id = generateId();
        repeatInterval = -1;  // -1 : 非周期性; 0:无限重复; >0 以该时间间隔重复
        isCancel = false;
    }

    private int generateId(){
        generateId += 1;
        Log.d(TAG,"Job id generated:" + generateId);
        return generateId;
    }

    // TODO: 2016/10/27 Job id的唯一性创建


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getMillisDelay() {
        return millisDelay;
    }

    public void setMillisDelay(long millisDelay) {
        this.millisDelay = millisDelay;
    }

    public Date getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Date executeTime) {
        this.executeTime = executeTime;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }

    public long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public boolean isCancel() {
        return isCancel;
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }
}
