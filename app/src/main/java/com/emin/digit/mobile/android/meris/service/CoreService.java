package com.emin.digit.mobile.android.meris.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.emin.digit.mobile.android.meris.platform.components.job.JobManager;
import com.emin.digit.mobile.android.meris.platform.components.timer.TimerManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samson on 2016/10/19.
 */
public class CoreService extends Service {

    private static final String TAG = CoreService.class.getSimpleName();

    private Handler serviceHandler = null;
    private CounterTask counterTask = new CounterTask();

    private static List<Book> sBookList;

    private static List<Book> getBookList() {
        if(sBookList == null){
            sBookList = new ArrayList<Book>();
        }
        return sBookList;
    }

    // service lifecycle
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"CoreService onCreate ...pid:" + getPid());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy ...");
        if(serviceHandler != null){
            serviceHandler.removeCallbacks(counterTask);
            serviceHandler = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand ...");


        // serviceHandler = new Handler();
        // serviceHandler.postDelayed(counterTask,0);

        // 启动Timer引擎
        Log.d(TAG,"CoreService 启动Timer引擎 ....");
        setupTimerEngine();

        Log.d(TAG,"CoreService 启动Job引擎 ....");
        setupJobEngine();

        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    // - - - - - - - - - - service 进程与 app 进程通信接口
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    class MyBinder extends IMyAidlInterface.Stub {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
            Log.d(TAG,"doGet data from client:" + anInt + " " + aLong);
        }

        @Override
        public int getCounter() throws RemoteException {
            Log.d(TAG,"getCounter:" + count);
            return count;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            if(book != null){
                Log.d(TAG,"book'name :" + book.getName() + " book's author:" + book.getAuthor());
                getBookList().add(book);
                //Log.d(TAG,"Book list size:" + sBookList.size());
                //printBookList();
            }
        }
    }

    // - - - - - - - - - - - -

    // 启动Timer引擎
    private void setupTimerEngine(){
        TimerManager.setup(this);
        TimerManager.getInstance().createTask("CoreService TimerTask1");
    }

    // 启动Job引擎
    private void setupJobEngine(){
        JobManager.setup(this);
    }


    private void printBookList(){
        for(int i = 0; i < sBookList.size(); i++){
            Log.d(TAG,"NO." + i + "name:" + sBookList.get(i).getName() + " author:" + sBookList.get(i).getAuthor());
        }
    }

    private int count = 0;
    private boolean stop = false;
    private boolean isFirst = true;
    private class CounterTask implements Runnable{
        @Override
        public void run() {
            count++;
            Log.d(TAG,"pid:" + getPid() + " CounterTask running..count:" +  count);
            //sendBookMessage();
            //sendReddotMessage();
            serviceHandler.postDelayed(counterTask,10000); // 5秒钟跑一次
            /*
            while (!stop){
                // stop = true;
            }*/
        }
    }

    // 发出REDDOT广播
    private void sendReddotMessage(){
        Log.d(TAG,"send reddot broadcast message");
        Intent intent = new Intent();
        intent.setAction("REDDOT");

        String message = "{\"templateid\":1," +
                "\"pageid\":\"reddot.html\"," +
                "\"itemid\":\"notPaid\"," +
                "\"status\":\"unread\"}";

        intent.putExtra("reddot",message);
        sendBroadcast(intent);
    }

    // 发出BOOK广播
    private void sendBookMessage(){
        Log.d(TAG,"send broadcast message");
        Intent intent = new Intent();
        intent.setAction("BOOK");
        // NG
        //intent.putExtra("reddot","number:2");

        // NG
        //Bundle bundle = new Bundle();
        //bundle.putString("reddot","888888");
        //intent.putExtras(bundle);

        // 传递复杂对象
        Bundle bundle = new Bundle();
        Book book = new Book();
        book.setName("JAY");
        book.setAuthor("Jay chou");
        bundle.putParcelable("book",book);
        intent.putExtras(bundle);

        sendBroadcast(intent);
    }

    private int getPid(){
        int pid = android.os.Process.myPid();
        return pid;
    }

    private long getThreadId(){
        long tid = Thread.currentThread().getId();
        return tid;
    }
}
