package com.emin.digit.mobile.android.meris.platform.plugin;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import com.emin.digit.mobile.android.meris.framework.database.DatabaseManager;
import com.emin.digit.mobile.android.meris.platform.core.PluginParams;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * 数据库插件
 *
 * Created by Samson on 16/8/1.
 */
public class PluginDatabase {

    private static final String TAG = PluginDatabase.class.getSimpleName();
    private static String dbNme = "meris.db";
    private static String dbPath = "/data/data/com.emin.digit.mobile.android.meris/databases/";

    /**
     * 初始化数据库,以table的JSON字符串创建数据表
     *
     * @param params 配置了数据表描述的JSON字符串的插件参数对象
     */
    public boolean initDatabase(PluginParams params) throws Exception {
        Context context = params.getWebView().getContext();
        String tablesJson = params.getArguments()[0];
        JSONObject tablesObj = new JSONObject(tablesJson);
        DatabaseManager.getInstance(context).createTable(tablesObj);
        return true;
    }

    /**
     * 通过原生SQL语句新增
     *
     * @param params 插件参数对象
     * @return boolean true:插入成功;出现异常则抛出由PluginManager捕获统一处理,返回false
     */
    public boolean insertWithSql(PluginParams params) throws Exception {
        Context context = params.getWebView().getContext();
        String sql = params.getArguments()[0];
        DatabaseManager.getInstance(context).execSQL(sql);
        return true;
    }

    /**
     * 通过原生SQL语句新增
     *
     * @param params 插件参数对象
     * @return boolean true:插入成功;出现异常则抛出由PluginManager捕获统一处理,返回false
     */
    public int insertWithSqlReturnId(PluginParams params) throws Exception {
        Context context = params.getWebView().getContext();
        String tableName = params.getArguments()[0];
        String sql = params.getArguments()[1];
        int id = 0;
        if(DatabaseManager.getInstance(context).execSQL(sql)){
            id = DatabaseManager.getInstance(context).getTableMaxId(tableName);
        }
        return id;
    }

    /**
     * 通过原生SQL语句查询
     *
     * @param params 插件参数对象
     *               js入参:如 [tableName,array]
     *               备注:其中的array,在js中可以这样:
     *               var array = new Array();
     *               array.push(memberObj); //memberObj是JSON对象,不是字符串
     * @return boolean true:插入成功;出现异常则抛出由PluginManager捕获统一处理返回false
     */
    public boolean insertWithJson(PluginParams params) throws Exception {
        Context context = params.getWebView().getContext();
        String tableName = params.getArguments()[0]; // 表名
        JSONArray jsonArray = new JSONArray(params.getArguments()[1]);
        DatabaseManager.getInstance(context).insert(tableName, jsonArray);
        return true;
    }

    /**
     * 通过原生SQL语句查询
     *
     * @param params 插件参数对象
     * @return
     */
    public String queryWithSql(PluginParams params) throws Exception {
        Context context = params.getWebView().getContext();
        String sql = params.getArguments()[0];
        JSONArray result = DatabaseManager.getInstance(context).queryWithSqlString(sql);
        return result.toString();
    }

    /**
     * 执行任意可执行的sql语句
     *
     * @param params 配置了可执行的sql语句参数的插件参数对象
     */
    public void executeSql(PluginParams params) {
        Context context = params.getWebView().getContext();
        String sql = params.getArguments()[0];
        DatabaseManager.getInstance(context).execSQL(sql);
    }

    /**
     * 批量执行Sql
     *
     * @param params
     * @return
     */
    public void execSqlBatch(PluginParams params) throws Exception {
        Context context = params.getWebView().getContext();
        JSONArray sqlArray = new JSONArray(params.getArguments()[1]);
        ArrayList<String> sqlList = new ArrayList<String>();
        for (int i = 0; i < sqlArray.length(); i++) {
            String sql = sqlArray.optString(i);
            sqlList.add(sql);
        }
        DatabaseManager.getInstance(context).execSqlBatch(sqlList);
    }

    /**
     * 更新数据表
     *
     * @param params 配置了sql语句的插件参数对象
     * @return
     */
    public boolean updateTable(PluginParams params) throws Exception {
        Context context = params.getWebView().getContext();
        String tablesJson = params.getArguments()[0];
        JSONObject tablesObj = new JSONObject(tablesJson);
        DatabaseManager.getInstance(context).updateTable(tablesObj);
        return true;
    }

    /**
     * 获取表的最大id
     *
     * @param params 配置了sql语句的插件参数对象
     * @return id
     * @throws Exception
     */
    public int getTableMaxId(PluginParams params) throws Exception{
        Context context = params.getWebView().getContext();
        String tableName = params.getArguments()[0];
        return DatabaseManager.getInstance(context).getTableMaxId(tableName);
    }

    //获取sdcard路径
    public void getSdCardDir() {
        String sdCardPath = Environment.getExternalStorageDirectory().toString();
        Log.d(TAG, "sdCardPath :" + sdCardPath);
    }
}
