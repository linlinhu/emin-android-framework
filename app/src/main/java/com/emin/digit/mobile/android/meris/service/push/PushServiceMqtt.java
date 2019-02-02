package com.emin.digit.mobile.android.meris.service.push;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.emin.digit.mobile.android.meris.R;
import com.emin.digit.mobile.android.meris.platform.core.EMHybridActivity;
import com.ibm.mqtt.IMqttClient;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttPersistence;
import com.ibm.mqtt.MqttPersistenceException;
import com.ibm.mqtt.MqttSimpleCallback;

import java.io.IOException;

/**
 * Created by Samson on 16/7/11.
 */
public class PushServiceMqtt extends Service{

    // this is the log tag
    public static final String		TAG = PushServiceMqtt.class.getSimpleName();

    // the IP address, where your MQTT broker is running.
    private static final String		MQTT_HOST = "192.168.0.51"; //"127.0.0.1"; //

    // the port at which the broker is running.
    private static int				MQTT_BROKER_PORT_NUM      = 1883;

    // Let's not use the MQTT persistence.

    private static MqttPersistence MQTT_PERSISTENCE          = null;

    // We don't need to remember any state between the connections, so we use a clean start.
    private static boolean			MQTT_CLEAN_START          = true;

    // Let's set the internal keep alive for MQTT to 15 mins. I haven't tested this value much. It could probably be increased.
    private static short			MQTT_KEEP_ALIVE           = 60 * 15;

    // Set quality of services to 0 (at most once delivery), since we don't want push notifications
    // arrive more than once. However, this means that some messages might doGet lost (delivery is not guaranteed)
    private static int[]			MQTT_QUALITIES_OF_SERVICE = { 0 } ;
    private static int				MQTT_QUALITY_OF_SERVICE   = 0;

    // The broker should not retain any messages.
    private static boolean			MQTT_RETAINED_PUBLISH     = false;

    // MQTT client ID, which is given the broker. In this example, I also use this for the topic header.
    // You can use this to run push notifications for multiple apps with one MQTT broker.
    public static String			MQTT_CLIENT_ID = "meris";

    // These are the actions for the service (name are descriptive enough)
    private static final String		ACTION_START = MQTT_CLIENT_ID + ".START";
    private static final String		ACTION_STOP = MQTT_CLIENT_ID + ".STOP";
    private static final String		ACTION_KEEPALIVE = MQTT_CLIENT_ID + ".KEEP_ALIVE";
    private static final String		ACTION_RECONNECT = MQTT_CLIENT_ID + ".RECONNECT";

    // Connection log for the push service. Good for debugging.
    //private ConnectionLog 			mLog;

    // Connectivity manager to determining, when the phone loses connection
    private ConnectivityManager mConnMan;
    // Notification manager to displaying arrived push notifications
    private NotificationManager mNotifMan;

    // Whether or not the service has been started.
    private boolean 				mStarted;

    // This the application level keep-alive interval, that is used by the AlarmManager
    // to keep the connection active, even when the device goes to sleep.
    private static final long		KEEP_ALIVE_INTERVAL = 1000 * 60 * 28;

    // Retry intervals, when the connection is lost.
    private static final long		INITIAL_RETRY_INTERVAL = 1000 * 10;
    private static final long		MAXIMUM_RETRY_INTERVAL = 1000 * 60 * 30;

    // Preferences instance
    private SharedPreferences mPrefs;

    // We store in the preferences, whether or not the service has been started
    public static final String		PREF_STARTED = "isStarted";

    // We also store the deviceID (target)
    public static final String		PREF_DEVICE_ID = "deviceID";

    // We store the last retry interval
    public static final String		PREF_RETRY = "retryInterval";

    // Notification title
    public static String			NOTIF_TITLE = "Emin-digit";

    // Notification id
    private static final int		NOTIF_CONNECTED = 0;
    //通知栏消息id,如果id不变,则消息栏就只有一个通知,内容自动覆盖
    private int messageNotificationID = 1000;

    // This is the instance of an MQTT connection.
    private MQTTConnection			mConnection;

    private long					mStartTime;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Static method to start the service
    public static void actionStart(Context ctx) {
        Log.i(TAG,"actionStart = = =1");
        Intent i = new Intent(ctx, PushServiceMqtt.class);
        i.setAction(ACTION_START);
        ctx.startService(i);
    }

    // Static method to stop the service
    public static void actionStop(Context ctx) {
        Log.i(TAG,"actionStop = = = 111");
        Intent i = new Intent(ctx, PushServiceMqtt.class);
        i.setAction(ACTION_STOP);
        ctx.startService(i);
    }

    // Static method to send a keep alive message
    public static void actionPing(Context ctx) {
        Intent i = new Intent(ctx, PushServiceMqtt.class);
        i.setAction(ACTION_KEEPALIVE);
        ctx.startService(i);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate === === === ");
        log("Creating service");
        mStartTime = System.currentTimeMillis();

//        try {
//            mLog = new ConnectionLog();
//            Log.i(TAG, "Opened log at " + mLog.getPath());
//        } catch (IOException e) {
//            Log.e(TAG, "Failed to open log", e);
//        }

        // Get instances of preferences, connectivity manager and notification manager
        mPrefs = getSharedPreferences(TAG, MODE_PRIVATE);
        mConnMan = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        mNotifMan = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		// If our process was reaped by the system for any reason we need
		// to restore our state with merely a call to onCreate.  We record
        // the last "started" value and restore it here if necessary.
        handleCrashedService();
    }

    // This method does any necessary clean-up need in case the server has been destroyed by the system
    // and then restarted
    private void handleCrashedService() {
        if (wasStarted() == true) {
            Log.i(TAG,"handleCrashedService wasStarted");
            log("Handling crashed service...");
            // stop the keep alives
            stopKeepAlives();

            // Do a clean start

            new Thread(new Runnable() {
                @Override
                public void run() {
                    start();
                }
            }).start();

        }else {
            Log.i(TAG,"handleCrashedService wasNotStarted");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand === === === ");
        log("Service started with intent=" + intent + "startId:" + startId);

        // Do an appropriate action based on the intent.
        if (intent.getAction().equals(ACTION_STOP) == true) {
            stop();
            stopSelf();
        } else if (intent.getAction().equals(ACTION_START) == true) {
            start();
        } else if (intent.getAction().equals(ACTION_KEEPALIVE) == true) {
            keepAlive();
        } else if (intent.getAction().equals(ACTION_RECONNECT) == true) {
            if (isNetworkAvailable()) {
                reconnectIfNecessary();
            }
        }

        // TODO: 2016/10/26 测试该service进程与app进程进程的独立性
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        log("Service destroyed (started=" + mStarted + ")");

        // Stop the services, if it has been started
        if (mStarted == true) {
            stop();
        }

//        try {
//            if (mLog != null)
//                mLog.close();
//        } catch (IOException e) {}
    }

    // log helper function
    private void log(String message) {
        log(message, null);
    }

    private void log(String message, Throwable e) {
        if (e != null) {
            Log.e(TAG, message, e);

        } else {
            Log.i(TAG, message);
        }

//        if (mLog != null) {
//            try {
//                mLog.println(message);
//            } catch (IOException ex) {}
//        }
    }

    // Reads whether or not the service has been started from the preferences
    private boolean wasStarted() {
        return mPrefs.getBoolean(PREF_STARTED, false);
    }

    // Sets whether or not the services has been started in the preferences.
    private void setStarted(boolean started) {
        mPrefs.edit().putBoolean(PREF_STARTED, started).commit();
        mStarted = started;
    }

    private synchronized void start() {
        log("Starting service...");

        // Do nothing, if the service is already running.
        if (mStarted == true) {
            Log.w(TAG, "Attempt to start connection that is already active");
            return;
        }

        // Establish an MQTT connection
        connect();

        // Register a connectivity listener
        registerReceiver(mConnectivityChanged, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private synchronized void stop() {
        // Do nothing, if the service is not running.
        if (mStarted == false) {
            Log.w(TAG, "Attempt to stop connection not active.");
            return;
        }

        // Save stopped state in the preferences
        setStarted(false);

        // Remove the connectivity receiver
        unregisterReceiver(mConnectivityChanged);
        // Any existing reconnect timers should be removed, since we explicitly stopping the service.
        cancelReconnect();

        // Destroy the MQTT connection if there is one
        if (mConnection != null) {
            mConnection.disconnect();
            mConnection = null;
        }
    }

    private synchronized void connect() {
        log("Connecting...");
        // fetch the device ID from the preferences.
        String deviceID = mPrefs.getString(PREF_DEVICE_ID, null);
        Log.i(TAG,"MQTT connection with deviceID:" + deviceID);
        // Create a new connection only if the device id is not NULL
        if (deviceID == null) {
            log("Device ID not found.");
        } else {
            try {

                Log.d(TAG,"mConnection:" + mConnection);
                Log.d(TAG,"device id is not null ,will create MQTT connection.");
                mConnection = new MQTTConnection(MQTT_HOST, deviceID);

                Log.d(TAG,"mConnection creating finished..");
            } catch (MqttException e) {
                // Schedule a reconnect, if we failed to connect
                log("MqttException: " + (e.getMessage() != null ? e.getMessage() : "NULL"));
                if (isNetworkAvailable()) {
                    Log.i(TAG,"isNetworkAvailable");
                    scheduleReconnect(mStartTime);
                }else{
                    Log.i(TAG,"isNotNetworkAvailable");
                }
            }
            setStarted(true);
        }
    }

    private synchronized void keepAlive() {
        try {
            // Send a keep alive, if there is a connection.
            if (mStarted == true && mConnection != null) {
                mConnection.sendKeepAlive();
            }
        } catch (MqttException e) {
            log("MqttException: " + (e.getMessage() != null? e.getMessage(): "NULL"), e);

            mConnection.disconnect();
            mConnection = null;
            cancelReconnect();
        }
    }

    // Schedule application level keep-alives using the AlarmManager
    private void startKeepAlives() {
        Intent i = new Intent();
        i.setClass(this, PushServiceMqtt.class);
        i.setAction(ACTION_KEEPALIVE);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + KEEP_ALIVE_INTERVAL,
                KEEP_ALIVE_INTERVAL, pi);
    }

    // Remove all scheduled keep alives
    private void stopKeepAlives() {
        Intent i = new Intent();
        i.setClass(this, PushServiceMqtt.class);
        i.setAction(ACTION_KEEPALIVE);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmMgr.cancel(pi);
    }

    // We schedule a reconnect based on the starttime of the service
    public void scheduleReconnect(long startTime) {
        // the last keep-alive interval
        long interval = mPrefs.getLong(PREF_RETRY, INITIAL_RETRY_INTERVAL);

        // Calculate the elapsed time since the start
        long now = System.currentTimeMillis();
        long elapsed = now - startTime;


        // Set an appropriate interval based on the elapsed time since start
        if (elapsed < interval) {
            interval = Math.min(interval * 4, MAXIMUM_RETRY_INTERVAL);
        } else {
            interval = INITIAL_RETRY_INTERVAL;
        }

        log("Rescheduling connection in " + interval + "ms.");

        // Save the new internval
        mPrefs.edit().putLong(PREF_RETRY, interval).commit();

        // Schedule a reconnect using the alarm manager.
        Intent i = new Intent();
        i.setClass(this, PushServiceMqtt.class);
        i.setAction(ACTION_RECONNECT);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, now + interval, pi);
    }

    // Remove the scheduled reconnect
    public void cancelReconnect() {
        Intent i = new Intent();
        i.setClass(this, PushServiceMqtt.class);
        i.setAction(ACTION_RECONNECT);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmMgr.cancel(pi);
    }

    private synchronized void reconnectIfNecessary() {
        if (mStarted == true && mConnection == null) {
            log("Reconnecting...");
            connect();
        }
    }

    // This receiver listeners for network changes and updates the MQTT connection
    // accordingly
    private BroadcastReceiver mConnectivityChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get network info
            NetworkInfo info = (NetworkInfo)intent.getParcelableExtra (ConnectivityManager.EXTRA_NETWORK_INFO);

            // Is there connectivity?
            boolean hasConnectivity = (info != null && info.isConnected()) ? true : false;

            log("Connectivity changed: connected=" + hasConnectivity);

            if (hasConnectivity) {
                reconnectIfNecessary();
            } else if (mConnection != null) {
                // if there no connectivity, make sure MQTT connection is destroyed
                mConnection.disconnect();
                cancelReconnect();
                mConnection = null;
            }
        }
    };



    // Add by samson 2016-07-11
    // API Level 16+ Notification API
    // Display the topbar notification
    private void showNotification(String text){

        //Notification notification = new Notification(R.drawable.icon,"meris push",System.currentTimeMillis());

        // 构建Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.icon); // 图标
        builder.setTicker(NOTIF_TITLE); // 接到推送时的横幅显示的信息,不一定是内容和标题
        builder.setContentTitle(NOTIF_TITLE); // 标题
        builder.setContentText(text); //内容
        builder.setAutoCancel(true); // 点击之后自动消失,否则一直在,直到滑动删除


        // 配置pendingIntent(即点击后进入app的某个Activity)
        Intent intent = new Intent(this,EMHybridActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);

        // 生成Notification
        Notification notification = builder.build();

        // 发送notification
//        mNotifMan.notify(NOTIF_CONNECTED,notification);
        Log.d(TAG,"messageNotificationID:" + messageNotificationID);
        mNotifMan.notify(messageNotificationID,notification);
        messageNotificationID++;
    }


    // TODO: 2016/10/26 计划改善成的形式
    private Notification buildNotification(Context ctx,String ticker, String title,
                                           String content, Intent intent, int intentId){
        // 构建Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.icon); // 图标
        builder.setTicker(ticker); // 接到推送时的横幅显示的信息,不一定是内容和标题
        builder.setContentTitle(title); // 标题
        builder.setContentText(content); //内容
        builder.setAutoCancel(true); // 点击之后自动消失,否则一直在,直到滑动删除

        // 配置pendingIntent(即点击后进入app的某个Activity)
        PendingIntent pendingIntent = PendingIntent.getActivity(this, intentId, intent, 0);
        builder.setContentIntent(pendingIntent);
        // 生成Notification
        Notification notification = builder.build();
        return notification;
    }

    // Check if we are online
    private boolean isNetworkAvailable() {
        NetworkInfo info = mConnMan.getActiveNetworkInfo();
        if (info == null) {
            return false;
        }
        return info.isConnected();
    }

    // This inner class is a wrapper on top of MQTT client.
    private class MQTTConnection implements MqttSimpleCallback {
        IMqttClient mqttClient = null;

        // Creates a new connection given the broker address and initial topic
        public MQTTConnection(String brokerHostName, String initTopic) throws MqttException {
            Log.d(TAG,"Creating MQTTConnection :" + brokerHostName + " thread id:" + Thread.currentThread().getId());
            // Create connection spec
            String mqttConnSpec = "tcp://" + brokerHostName + "@" + MQTT_BROKER_PORT_NUM;

            // Create the client and connect
            mqttClient = MqttClient.createMqttClient(mqttConnSpec, MQTT_PERSISTENCE);
            Log.d(TAG,"created MqttClient success:" + mqttClient);
            if(mqttClient.isConnected()){
                Log.d(TAG,"mqttClient is connected!");
            }else {
                Log.d(TAG,"mqttClient is not connected!");
            }

            String clientID = MQTT_CLIENT_ID + "/" + mPrefs.getString(PREF_DEVICE_ID, "");
            Log.d(TAG,"connect client id:" + clientID);

            try {
                mqttClient.connect(clientID, MQTT_CLEAN_START, MQTT_KEEP_ALIVE);
            } catch (MqttException e) {
                Log.d(TAG,"exception occurred");
                e.printStackTrace();
            }
            Log.d(TAG,"client connected success");

            // register this client app has being able to receive messages
            mqttClient.registerSimpleHandler(this);

            // Subscribe to an initial topic, which is combination of client ID and device ID.
            initTopic = MQTT_CLIENT_ID + "/" + initTopic;
            //subscribeToTopic(initTopic);
            subscribeToTopic("123456789");
            log("Connection established to " + brokerHostName + " on topic " + initTopic);

            // Save start time
            mStartTime = System.currentTimeMillis();
            // Star the keep-alives
            startKeepAlives();
        }

        // Disconnect
        public void disconnect() {
            try {
                stopKeepAlives();
                mqttClient.disconnect();
            } catch (MqttPersistenceException e) {
                log("MqttException" + (e.getMessage() != null? e.getMessage():" NULL"), e);
            }
        }
        /*
         * Send a request to the message broker to be sent messages published with
         *  the specified topic name. Wildcards are allowed.
         */
        private void subscribeToTopic(String topicName) throws MqttException {
            Log.i(TAG,"subscribe topic :" + topicName);
            if ((mqttClient == null) || (mqttClient.isConnected() == false)) {
                // quick sanity check - don't try and subscribe if we don't have
                // a connection
                log("Connection error" + "No connection");
            } else {
                String[] topics = { topicName };
                mqttClient.subscribe(topics, MQTT_QUALITIES_OF_SERVICE);
                Log.i(TAG,"mqttClient is not null");
            }
        }
        /*
         * Sends a message to the message broker, requesting that it be published
         * to the specified topic.
         */
        private void publishToTopic(String topicName, String message) throws MqttException {
            if ((mqttClient == null) || (mqttClient.isConnected() == false)) {
                // quick sanity check - don't try and publish if we don't have
                //  a connection
                log("No connection to public to");
            } else {
                mqttClient.publish(topicName, message.getBytes(), MQTT_QUALITY_OF_SERVICE, MQTT_RETAINED_PUBLISH);
            }
        }

        /*
         * Called if the application loses it's connection to the message broker.
         */
        public void connectionLost() throws Exception {
            log("Loss of connection" + "connection downed");
            stopKeepAlives();
            // null itself
            mConnection = null;
            if (isNetworkAvailable() == true) {
                reconnectIfNecessary();
            }
        }

        /*
         * Called when we receive a message from the message broker.
         */
        public void publishArrived(String topicName, byte[] payload, int qos, boolean retained) {
            Log.i(TAG,"publishArrived ");

            // Show a notification
            String s = new String(payload);
            log("Got message: " + s);
            showNotification(s);
            sendReddotMessage();
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

        public void sendKeepAlive() throws MqttException {
            log("Sending keep alive");
            // publish to a keep-alive topic
            publishToTopic(MQTT_CLIENT_ID + "/keepalive", mPrefs.getString(PREF_DEVICE_ID, ""));
        }
    }
}
