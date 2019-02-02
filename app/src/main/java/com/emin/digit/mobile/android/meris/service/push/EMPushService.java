package com.emin.digit.mobile.android.meris.service.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.emin.digit.mobile.android.meris.R;
import com.emin.digit.mobile.android.meris.platform.core.EMHybridActivity;

/**
 * Created by Samson on 2016/10/21.
 */
public class EMPushService extends Service{

    private static final String TAG = EMPushService.class.getSimpleName();
    //获取消息线程
    private MessageThread messageThread = null;
    //点击查看
    private Intent messageIntent = null;
    private PendingIntent messagePendingIntent = null;
    //通知栏消息
    private int messageNotificationID = 1000;
    private Notification messageNotification = null;
    private NotificationManager messageNotificationManager = null;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("PushService","onStartCommand");
        //初始化
        messageNotificationManager = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        //开启线程
        MessageThread thread = new MessageThread();
        thread.isRunning = true;
        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    /***
     * 从服务端获取消息
     */
    class MessageThread extends Thread{
        //运行状态
        public boolean isRunning = true;
        @Override
        public void run() {
            Log.d("PushService","MessageThread run");
            while(isRunning){
                try {
                    //休息10秒
                    Thread.sleep(10000);
                    if(getServerMessage().equals("emin")){
                        Log.d("PushService","doGet push message from emin server...");
                        //设置消息内容和标题
                        //messageNotification.setLatestEventInfo(EMPushService.this, "您有新消息!", "这是一条新的测试消息", messagePendingIntent);
                        //发布消息
                        //messageNotificationManager.notify(messageNotificationID, messageNotification);
                        //避免覆盖消息，采取ID自增
                        //messageNotificationID++;

                        // 测试Notification
                        //showNotification("push content coming");

                        // 测试红点服务
                        sendReddotMessage();
                        //sendMessage();

                        isRunning = false;
                        //sendNotification();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // API Level 16+ Notification API
    private void showNotification(String text){

        // 构建Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.icon); // 图标
        builder.setTicker("Emin-ticker"); // 接到推送时的横幅显示的信息,不一定是内容和标题
        builder.setContentTitle("Emin-title"); // 标题
        builder.setContentText(text); //内容
        builder.setAutoCancel(true); // 点击之后自动消失,否则一直在,直到滑动删除

        // 配置pendingIntent(即点击后进入app的某个Activity)
        Intent intent = new Intent(this,EMHybridActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);

        // 生成Notification
        Notification notification = builder.build();

        // 发送notification
        messageNotificationManager.notify(messageNotificationID, notification);
        messageNotificationID++;
    }

    private void showNotification(){

        Notification n = new Notification();
        n.flags |= Notification.FLAG_SHOW_LIGHTS;
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        n.defaults = Notification.DEFAULT_ALL;
        n.icon = R.drawable.icon;
        n.when = System.currentTimeMillis();
        // Simply open the parent activity
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, EMHybridActivity.class), 0);
        // Change the name of the notification here
//		n.setLatestEventInfo(this, NOTIF_TITLE, text, pi);

        messageNotificationManager.notify(messageNotificationID, n);
        messageNotificationID++;
    }

    /***
     * 模拟了服务端的消息。实际应用中应该去服务器拿到message
     * @return
     */
    public String getServerMessage(){
        return "emin";
    }

    private void sendMessage(){
        Log.d(TAG,"send broadcast message");
        Intent intent = new Intent();
        intent.setAction("ABC");
        sendBroadcast(intent);
    }

    private void sendReddotMessage(){
        Log.d(TAG,"= = = = = = send broadcast message");

        String message = "{\"templateid\":1," +
                "\"pageid\":\"reddot.html\"," +
                "\"itemid\":\"notPaid\"," +
                "\"status\":\"unread\"}";
        Intent intent = new Intent();
        intent.setAction("REDDOT");
        intent.putExtra("reddot",message);
        sendBroadcast(intent);
    }
}
