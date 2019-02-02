package com.emin.digit.mobile.android.meris.platform.components.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 网络监听器
 * 设备的网络状态变更,会自动触发onReceive 方法
 *
 * Created by Samson on 2017/1/17.
 */
public class NetBroadcastReceiver extends BroadcastReceiver{
    private static final String TAG = NetBroadcastReceiver.class.getSimpleName();

    private static ArrayList<NetEventHandler> mListeners = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        // 网络状态发生变化,通知handler以执行回调
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            //EminApplication.mNetWorkState = NetUtil.getNetworkState(context);
            int netWorkState = NetUtil.getNetWorkState(context);
            if (mListeners.size() <= 0)
                return;

            // 该网络监听器可供任何一个需要监听网络状态的目标(如activity)响应
            for (NetEventHandler handler : mListeners) {
                handler.onNetStateChange(netWorkState);
            }
        }
    }

    /**
     * 新增监听器
     * 只有新增了监听器,才能执行js回调
     *
     * @param handler 处理网络状态变更的目标
     */
    public static void addListener(NetEventHandler handler){
        if(!mListeners.contains(handler)) {
            mListeners.add(handler);
        }
    }

    /**
     * 移除监听器
     * 监听器被移除之后,也就不会触发js回调
     *
     * @param handler 处理网络状态变更的目标
     */
    public static void removeListener(NetEventHandler handler){
        if(mListeners.contains(handler)){
            mListeners.remove(handler);
        }
    }

    public static List getListeners(){
        return mListeners;
    }

    /**
     * 通过接口实现网络状态变更的回调事件
     */
    public interface NetEventHandler {
        void onNetStateChange(int netType);
    }
}
