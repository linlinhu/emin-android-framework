package com.emin.digit.mobile.android.meris.platform.components.job;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Samson on 2016/10/26.
 *
 * AlarmManager实现精确定时操作
 */
public class JobManager {

    private static final String TAG = JobManager.class.getSimpleName();

    private Context mContext;
    private AlarmManager mAlarmManager;
    private static HashMap<Integer, PendingIntent> operationMap = null;
    private static HashMap<Integer, JobInfo> jobMap = null;

    private static JobManager instance = new JobManager();

    public static JobManager getInstance(){
        return instance;
    }

    private JobManager(){
    }

    public static void setup(Context context){
        instance.mContext = context;
        instance.mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(instance.operationMap == null){
            instance.operationMap = new HashMap<>();
        }
        if(instance.jobMap == null){
            instance.jobMap = new HashMap<>();
        }
    }

    // 平台业务的Job
    /**
     * 创建自定义Job
     *
     * 1.延迟多少时间后执行的Job
     * 2.到达某个时间点执行的Job
     *   A.某个时间点非周期性的Job(本质上跟1是一样的,指定的时间与当前时间的毫秒数的差就是延迟的时间)
     *   B.某个时间点周期性的Job
     *     B1. 某个时间点无限重复的job
     *     B2. 某个时间点在某个时间段内重复的Job
     *
     * @param jobInfo
     */
    public void createJob(JobInfo jobInfo){
        printJobInfo(jobInfo);

        int jobId = jobInfo.getId();
        String name = jobInfo.getName();
        // 延迟几毫秒执行,和某个特定的时间点执行,本质都是延迟执行,这种类型的Job只执行一次
        if(jobInfo.getMillisDelay() != -1){
            createJobDelay(jobId, name, jobInfo.getMillisDelay());
        }else{
            Log.d(TAG,"非延迟几毫秒执行的job");
            Date jobExecuteTime = jobInfo.getExecuteTime();
            if(jobExecuteTime != null){
                // job的周期性
                long repeatInterval = jobInfo.getRepeatInterval();
                if(repeatInterval < 0){
                    Log.d(TAG,"这是一个非周期性执行的Job");

                    long targetMillis = jobExecuteTime.getTime();
                    long currentMillis = System.currentTimeMillis();

                    Log.d(TAG,"target date to execute:" + jobExecuteTime);
                    Log.d(TAG,"target millis:" + targetMillis);
                    Log.d(TAG,"current millis:" + currentMillis);

                    long distanceMillis = targetMillis - currentMillis;
                    Log.d(TAG,"distance Millis :" + distanceMillis);

                    if(distanceMillis > 0){
                        createJobDelay(jobId, name,distanceMillis);
                    }else{
                        Log.d(TAG,"这是一个无效的时间,目标时间是一个过去的时间点");
                    }

                }else{
                    Log.d(TAG,"这是一个周期性执行的Job");
                    //long startTime = System.currentTimeMillis();
                    long startTime = jobExecuteTime.getTime();
                    createJobRepeat(jobId, name, startTime, repeatInterval);
                }
            }else{
                Log.d(TAG,"无法识别的job");
            }
        }

    }

    // TODO: 2016/10/28 1.执行的时间点周期的稳定性；2.重复时间的间隔的最小单元问题(1分钟没问题,30秒有问题)
    /**
     * 创建一个只延迟 millisDelay 时间的Job,只执行一次
     *
     * @param name
     * @param millisDelay 延迟的毫秒数
     */
    private void createJobDelay(int jobId, String name, long millisDelay){
        Intent intent = new Intent(mContext,JobReceiver.class);
        intent.setAction(name);
        PendingIntent operation = PendingIntent.getBroadcast(mContext, jobId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        operationMap.put(Integer.valueOf(jobId),operation); // 保存该PendingIntent以便后续可以取消它
        long triggerAtMillis = System.currentTimeMillis() + millisDelay;
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
    }

    /**
     * 创建从以startTime 开始,间隔 intervalTime (毫秒数),周期性执行的Job
     *
     * @param name
     * @param startTime
     * @param intervalTime
     */
    private void createJobRepeat(int jobId, String name, long startTime, long intervalTime){
        Intent intent = new Intent(mContext,JobReceiver.class);
        intent.setAction(name);
        PendingIntent operation = PendingIntent.getBroadcast(mContext, jobId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d(TAG,"operation PendingIntent:" + operation);
        Log.d(TAG,"AlarmManager setRepeating: method called date" + new Date());
        Log.d(TAG,"AlarmManager setRepeating: param startTime:" + startTime);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTime, intervalTime, operation);
        //startTime = SystemClock.elapsedRealtime();
        //mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, startTime, intervalTime, sender);
        operationMap.put(Integer.valueOf(jobId),operation); // 保存该PendingIntent以便后续可以取消它
    }

    /**
     * 取消Job,停止执行,但并没有移除{@link #removeJob(int)},将来可以重新启动
     *
     * 通过AlarmManager 取消某个定时任务,得一对一确定PendingIntent(API 中的requestCode)
     * 这里用JobId进行标识
     * 备注:JobId的唯一性,在创建{@link JobInfo}时生成的id规则
     *
     * @param jobId 任务的标识
     */
    public boolean cancelJob(int jobId){
        PendingIntent operation = operationMap.get(jobId);
        Log.d(TAG," operation PendingIntent:" + operation);
        mAlarmManager.cancel(operation);
        operationMap.remove(jobId);
        return true;
    }

    /**
     * 移除job
     *
     * @param jobId
     */
    public void removeJob(int jobId){

    }

    public Set<Integer> getJobIdSet(){
        return operationMap.keySet().size() == 0 ? null :operationMap.keySet();
    }

    public static class JobReceiver extends BroadcastReceiver{
        public JobReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"JobReceiver Receive action:" + intent.getAction() + " date:" + new Date().toString() + " Thread id:" + Thread.currentThread().getId());
        }
    }

    private void printJobInfo(JobInfo jobInfo){
        Log.d(TAG,"job id:" + jobInfo.getId());
        Log.d(TAG,"job name:" + jobInfo.getName());
        Log.d(TAG,"job millis delay:" + jobInfo.getMillisDelay());
        Log.d(TAG,"job execute time:" + jobInfo.getExecuteTime());
        Log.d(TAG,"start date:" + jobInfo.getStartDate());
        Log.d(TAG,"job end date:" + jobInfo.getEndDate());
        Log.d(TAG,"job repeatType:" + jobInfo.getRepeatType());
        Log.d(TAG,"job repeat interval:" + jobInfo.getRepeatInterval());
    }
}
