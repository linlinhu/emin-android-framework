package com.emin.digit.mobile.android.meris.platform.components.timer;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.emin.digit.mobile.android.meris.platform.utils.UUIDGenerator;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Samson on 2016/10/28.
 */
public class TimerManager {

    private static final String TAG = TimerManager.class.getSimpleName();

    private Context mContext;
    private static TimerManager instance = new TimerManager();

    private TimerManager(){
    }

    public static TimerManager getInstance(){
        return instance;
    }
    // TODO: 2016/10/28 将TimerTask单独封装出来
    private static Timer mTimer;

    private static HashMap<String,TimerTask> mTaskMap;

    public static void setup(Context context){
        instance.mContext = context;
        if(mTimer == null){
            instance.mTimer = new Timer(true);
        }
        if(mTaskMap == null){
            instance.mTaskMap = new HashMap<>();
        }
    }

    // TODO: 2016/10/28 TimerTask id 生成的唯一性机制的建立(同Job引擎的JobId)
    private static int id = 1;

    private String generateTaskId(){
        return UUIDGenerator.genUUID(16);
    }

    /**
     * timer task creating
     */
    public void createTask(final String name){
        if(mTimer == null){
            return;
        }
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //int pid = android.os.Process.myPid();
                //long threadId = Thread.currentThread().getId();
                Message message = Message.obtain();
                message.what = 1000;
                taskHandler.sendMessage(message);
            }
        };
        mTimer.schedule(task,2000,10000);
        mTaskMap.put(generateTaskId(), task);
    }

    public boolean cancelTask(String taskId){
        TimerTask task = getTaskWithId(taskId);
        if(task != null){
            task.cancel();
            return true;
        }
        return false;
    }

    private TimerTask getTaskWithId(String taskId){
        TimerTask task = mTaskMap.get(taskId);
        if(task != null){
            return task;
        }
        return null;
    }

    // - - - - - - - - 可能需要封装TimerTask这个抽象类
    public void remove(String taskId) {
        this.cancelTask(taskId);
        mTaskMap.remove(taskId);
    }

    /**
     * TimerTask运行在子线程中,通过Handler与主线程进行交互
     */
    private Handler taskHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int msgId = msg.what;
            switch (msgId) {
                case 1000:
                    sendReddotMessage();
                    break;
                default:
                    break;
            }
        }
    };

    // 发出REDDOT广播
    private void sendReddotMessage(){
        Log.d(TAG,"send reddot broadcast message");
        int pid = android.os.Process.myPid();
        long threadId = Thread.currentThread().getId();
        Log.d(TAG,"process id:" + pid + " thread id:" + threadId);

        Intent intent = new Intent();
        intent.setAction("REDDOT");

        String message = "{\"templateid\":1," +
                "\"pageid\":\"reddot.html\"," +
                "\"itemid\":\"notPaid\"," +
                "\"status\":\"unread\"}";

        intent.putExtra("reddot",message);
        mContext.sendBroadcast(intent);
    }

}
