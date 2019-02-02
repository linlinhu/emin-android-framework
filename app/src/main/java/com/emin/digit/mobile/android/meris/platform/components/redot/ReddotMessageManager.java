package com.emin.digit.mobile.android.meris.platform.components.redot;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.emin.digit.mobile.android.meris.framework.database.DatabaseManager;
import com.emin.digit.mobile.android.meris.framework.database.sqlite.SqliteManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Samson on 2016/10/13.
 *
 * 红点消息管理器,负责处理新消息(新增)/读取消息(删除),计算提醒数量以及红点数量的层级汇总,并更新数据库
 *
 * Bug fixed:sql语句中的数字加引号,查询不出数据
 * 注意:数字不能加[引号],用数据库工具查询都没问题,但是在程序中有差异！以下个sql,前者OK,后者NG
 *  select reddotid,number from reddotRecord where templateid= 1 and pageid='reddot.html' and itemid='order'
 *  select reddotid,number from reddotRecord where templateid='1' and pageid='reddot.html' and itemid='order'
 */
public class ReddotMessageManager {

    private static final String TAG = ReddotMessageManager.class.getSimpleName();

    private Context mContext;

    // 单例饿汉模式保证线程的安全性
    private static ReddotMessageManager instance = new ReddotMessageManager();

    public static ReddotMessageManager getInstance(Context context) {
        if(instance.mContext != context){
            instance.mContext = context;
        }
        return instance;
    }

    private ReddotMessageManager() {
    }

    // - - - - - 红点消息json,
    private static final String sMessageStatus = "status";
    private static final String sMessageStatusRead = "read";
    private static final String sMessageStatusUnRead = "unread";

    /**
     * 处理红点消息
     * 未读消息过来,红点数量执行加操作
     * 已读消息过来,红点数量执行减操作
     *
     * @param msgJson 红点消息的JSON字符串
     * @throws JSONException  JSON异常
     */
    public void dispatchReddotMessage(String msgJson) throws Exception {
        tempReddotNumber = 0; // 清空临时变量,避免下一次操作有残留影响
        JSONObject message = new JSONObject(msgJson);
        String reddotId = getReddotId(message);
        String status = message.optString(sMessageStatus);

        if(status.trim().equalsIgnoreCase(sMessageStatusUnRead)){
            unreadMessage(reddotId);
        } else if (status.trim().equalsIgnoreCase(sMessageStatusRead)){
            readMessage(reddotId);
        } else {
            Log.d(TAG,"undefined message status:" + status);
        }
    }

    // read message
    private void readMessage(String reddotId) throws Exception {
        calculateReddotCount(reddotId,sMessageStatusRead);
    }

    // new message
    private void unreadMessage(String reddotId) throws Exception {
        calculateReddotCount(reddotId,sMessageStatusUnRead);
    }

    // TODO: 2016/10/24 红点汇总关系的数量的一致性保证问题
    // 递归计算有红点层次的红点数量,如果无parent,则递归结束
    private void calculateReddotCount(String reddotId, final String status) throws Exception {

        // 查询上级id,并将自身number关联给临时红点数量
        String parentId = getParentReddotId(reddotId);
        Log.d(TAG,reddotId  + "'s parentId :" + parentId);

        if(status.trim().equalsIgnoreCase(sMessageStatusUnRead)) {
            plusReddotNumber(reddotId);
        } else if(status.trim().equalsIgnoreCase(sMessageStatusRead)) {
            //Log.d(TAG,"计算读消息，即将执行提醒数量减操作");
            if(tempReddotNumber <= 0) {
                return;
            }
            reduceReddotNumber(reddotId);
        } else {
            // TODO: 2016/10/31 最好抛出异常
            Log.d(TAG,"undefined message status:" + status);
        }

        if(!TextUtils.isEmpty(parentId) && !parentId.trim().equalsIgnoreCase("null")) {
            calculateReddotCount(parentId, status);
        } else {
            //Log.d(TAG, reddotId + " has no parent");
        }
    }

    // 自我保护功能.在读取消息(数量的减操作)时候,如果原来的number已经是0,则不必做减操作,也不必递归它的上一级
    private int tempReddotNumber = 0;

    private static final String sTableReddotRecord = "reddotRecord"; // 红点记录表
    private static final String sTableReddotTree   = "reddotTree";  // 红点汇合关系表

    private static final String sReddotId   = "reddotid";
    private static final String sTemplateId = "templateid";
    private static final String sPageId     = "pageid";
    private static final String sItemId     = "itemid";
    private static final String sNumber     = "number";
    private static final String sParentId   = "parentid";

    // 解析推送过来的消息,获取reddotid
    private String getReddotId(JSONObject message) throws Exception {
        String templateId = message.optString(sTemplateId);
        String pageId = message.optString(sPageId);
        String itemId = message.optString(sItemId);
        String reddotId = null;
        //Log.d(TAG,"templateId:" + templateId + " pageId:"+ pageId + " itemId:" + itemId);

        if(mContext != null){
            Log.d(TAG,"mContext is not null :" + mContext);
            String sql = "select "+ sReddotId + "," + sNumber
                    + " from " + sTableReddotRecord
                    + " where " + sTemplateId + " = " + templateId
                    + " and " + sPageId + " = '" + pageId + "'"
                    + " and " + sItemId + " = '" + itemId + "'";
            Log.d(TAG,"sql:" + sql);

//            JSONArray array = DatabaseManager.getInstance(mContext).queryWithSqlString(sql);
            JSONArray array = SqliteManager.getInstance(mContext).query(sql);
            JSONObject obj = array.optJSONObject(0);
            if(obj != null){
                reddotId = obj.optString(sReddotId);
                String number = obj.optString(sNumber);
                tempReddotNumber = Integer.parseInt(number);
            }
        }
        return reddotId;
    }

    // 从红点汇合关系表中找出上一级,并加1/减1
    private String getParentReddotId(String reddotId) throws Exception {
        String sql = "select " + sParentId
                + " from " + sTableReddotTree
                + " where " + sReddotId + " = " + reddotId;
        String parentId = null;
//        JSONArray array = DatabaseManager.getInstance(mContext).queryWithSqlString(sql);
        JSONArray array = SqliteManager.getInstance(mContext).query(sql);
        JSONObject obj = array.optJSONObject(0);
        if(obj != null){
            parentId = obj.optString(sParentId);
        }
        return parentId;
    }

    // 红点数量加1
    private void plusReddotNumber(String reddotId){
        // update reddotRecord set number=number+1 where reddotid=1
        String sql = "update " + sTableReddotRecord
                + " set " + sNumber + " = " + sNumber + " + 1"
                + " where " + sReddotId + " = " + reddotId;
        DatabaseManager.getInstance(mContext).execSQL(sql);
    }

    // 红点数量减1
    private void reduceReddotNumber(String reddotId){
        // update reddotRecord set number=number-1 where reddotid=1 and number>0
        String sql = "update " + sTableReddotRecord
                + " set " + sNumber + " = " + sNumber + " - 1"
                + " where " + sReddotId + " = " + reddotId
                + " and " + sNumber + " > 0";
        DatabaseManager.getInstance(mContext).execSQL(sql);
    }
}
