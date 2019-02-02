package com.emin.digit.mobile.android.meris.platform.plugin;

import android.content.Context;
import android.util.Log;

import com.emin.digit.mobile.android.meris.platform.components.job.JobManager;
import com.emin.digit.mobile.android.meris.platform.components.job.JobInfo;
import com.emin.digit.mobile.android.meris.platform.core.PluginParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Samson on 2016/10/26.
 *
 * Job引擎插件,供web前端主动调用,管理任务
 */
public class PluginJob {

    private static final String TAG = PluginJob.class.getSimpleName();

    public void startup(PluginParams params){
        Context context = params.getWebView().getContext();
        JobManager.getInstance().setup(context);
    }

    public void create(PluginParams params){
        String jobJson = params.getArguments()[0];
        JSONObject job = null;
        JobInfo jobInfo = new JobInfo();
        try {
            job = new JSONObject(jobJson);
            jobInfo.setName(job.optString("name"));

            // 一个延迟多少毫秒后执行的job
            if(job.has("millisDelay")){
                long millisDelay = Long.parseLong(job.optString("millisDelay"));
                jobInfo.setMillisDelay(millisDelay);
            }else {
                jobInfo.setMillisDelay(-1);
            }

            // 一个指定时间点执行的job
            if(job.has("executeTime")){
                String time = job.optString("executeTime");
                Date date = parseDate(time);
                jobInfo.setExecuteTime(date);
            }

            if(job.has("repeatInterval")){
                long interval = Long.parseLong(job.optString("repeatInterval"));
                jobInfo.setRepeatInterval(interval);
            }else {
                jobInfo.setRepeatInterval(-1); // 在JobInfo的构造方式中也配置了默认值-1,表示不重复
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NumberFormatException e){
            Log.d(TAG,"Job延迟时间的格式出错!");
            e.printStackTrace();
        }
        JobManager.getInstance().createJob(jobInfo);
    }

    public boolean cancel(PluginParams params){
        Log.d(TAG,"cancel");
        String jobIdStr = params.getArguments()[0];
        Log.d(TAG,"Should cancel job with id:" + jobIdStr);
        int jobId = 0;
        try {
            jobId = Integer.parseInt(jobIdStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return JobManager.getInstance().cancelJob(jobId);
    }

    public void getJobIds(PluginParams params){
        Set idSet = JobManager.getInstance().getJobIdSet();
        Log.d(TAG,"idSet:" + idSet);
        if(idSet!=null && idSet.size() > 0){
            JSONArray array = new JSONArray();
            Iterator it = idSet.iterator();
            while (it.hasNext()){
                String id = it.next().toString();
                Log.d(TAG,"job id:" + id);
            }
        }else {
            Log.d(TAG,"job id set has no element");
        }

    }

    private Date parseDate(String dateString){
        Log.d(TAG,"parse date:" + dateString);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = null;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
