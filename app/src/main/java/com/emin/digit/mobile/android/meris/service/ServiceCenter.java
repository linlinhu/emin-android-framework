package com.emin.digit.mobile.android.meris.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import com.emin.digit.mobile.android.meris.platform.core.EMHybridActivity;
import com.emin.digit.mobile.android.meris.platform.components.redot.ReddotManager;
import com.emin.digit.mobile.android.meris.service.push.PushServiceMqtt;

import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Samson on 2016/10/21.
 * 关于service保活机制
 * 建议研究TCP长链接的方式实现service保活
 * http://www.jianshu.com/p/06a1a434e057
 * http://blog.csdn.net/lhd201006/article/details/50698211
 *
 */
public class ServiceCenter {
    private static final String TAG = ServiceCenter.class.getSimpleName();

    private Activity activity;

    private IMyAidlInterface remoteService = null;
    private ServiceTestConnection conn = null;

    public ServiceCenter(EMHybridActivity activity){
        this.activity = activity;
    }

    // TODO: 2016/10/26 待优化,广播监听的不重复性保证问题
    public void startupPushService(){
        startupPushService(activity);

        startBroadcastListen();
    }

    public void startupPushService(Context ctx){
        String deviceID = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
        SharedPreferences.Editor editor = ctx.getSharedPreferences(PushServiceMqtt.TAG,Context.MODE_PRIVATE).edit();
        editor.putString(PushServiceMqtt.PREF_DEVICE_ID,deviceID);
        editor.commit();
        PushServiceMqtt.actionStart(ctx);

    }

    public void startUpService(Class serviceClass){
        int pid = android.os.Process.myPid();
        startService(serviceClass);
        bindService(serviceClass);

        // 测试client进程调用service进程的方法
        // startTimer();
        // 测试service进程给client发送消息,通过广播的方式接收推送过来的消息
        startBroadcastListen();
    }

    private void startService(Class serviceCls) {
        Intent intent = new Intent(activity.getApplicationContext(),serviceCls);
        activity.startService(intent);
    }

    private void bindService(Class serviceCls){
        if(conn == null){
            conn = new ServiceTestConnection();
            Intent intent = new Intent(activity.getApplicationContext(), serviceCls);
            //intent.setAction("com.emin.digit.mobile.android.meris.IMyAidlInterface");
            activity.bindService(intent, conn, Context.BIND_AUTO_CREATE);
        }
    }

    public void releaseService(Class serviceClass){
        if(conn != null){
            stopService(serviceClass);
            activity.unbindService(conn);
            conn = null;
        }
    }

    private void stopService(Class serviceClass){
        if(conn != null){
            Intent i = new Intent(activity.getApplicationContext() , serviceClass);
            activity.stopService(i);
        }
    }

    private IntentFilter reddotFilter = null;

    // 广播的监听
    private void startBroadcastListen(){
        //注册广播接收器（动态注册）
        if(reddotFilter == null){
            reddotFilter = new IntentFilter();
        }
        reddotFilter.addAction("REDDOT");
        activity.registerReceiver(broadcastCenter, reddotFilter);
    }

    public void stopBroadcastListen(){
        if(reddotFilter != null){
            activity.unregisterReceiver(broadcastCenter);
        }
    }

    /**
     * 广播中心
     * 负责处理进程内部/或者service进程的消息
     */
    BroadcastReceiver broadcastCenter = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG," receive broadcast action:" + action);
            if(action.equalsIgnoreCase("REDDOT")){
                String message = intent.getStringExtra("reddot");
                Log.d(TAG,"= = = = = = = = message form service:" + message);
                try {
                    ReddotManager.getInstance().reddotCounting(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(action.equalsIgnoreCase("book")) {
                Book book = (Book) intent.getParcelableExtra("book");
                if(book != null){
                    String name = book.getName();
                    Log.d(TAG,"book name:" + name);
                }

            }
        }
    };


    // - - - - - - - - - - - - 测试 app进程与service进程(CoreService)之间的交互
    private void startTimer(){
        Timer timer = new Timer(true);
        timer.schedule(task,0,5000); // 1秒钟之后,每隔5秒钟执行task
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            try {
                if(remoteService != null && conn != null){
                    int count  = remoteService.getCounter();
                    Log.d(TAG,"count from remote service:" + count);
                    if(count > 3){
                        // 测试从app进程传递自定义数据类对象(Book)到CoreService进程
                        bookRegister(count);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    // 进程间传输自定义类型的数据测试
    private void bookRegister(int n){
        Book book = new Book();
        book.setName("JAY" + n);
        book.setAuthor("Jay chou" + n);
        try {
            remoteService.addBook(book);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private class ServiceTestConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            remoteService = IMyAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            remoteService = null;
        }
    }

}
