package com.emin.digit.mobile.android.meris.platform.plugin;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.emin.digit.mobile.android.meris.framework.database.DaoConfig;
import com.emin.digit.mobile.android.meris.framework.database.sqlite.SqliteManager;
import com.emin.digit.mobile.android.meris.platform.core.PluginParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * author: Samson
 * created on: 2017/10/31 上午 11:23
 * description:
 * 前端操作数据库插件
 * 1.支持原生SQL语句操作数据库
 * 2.支持前端数据模型(数据对象)操作数据库
 *
 */
public class PluginOrm {

    private static final String TAG = PluginOrm.class.getSimpleName();

    /**
     * 创建数据库
     *
     * @param params 插件参数对象,其包装web前端数据库名称的字符串
     * @return true:成功;false失败
     */
    public boolean createDatabase(PluginParams params) {
        String dbName = params.getArguments()[0];
        DaoConfig daoConfig = new DaoConfig();
        daoConfig.setContext(params.getWebView().getContext());
        if(!TextUtils.isEmpty(dbName)) {
            daoConfig.setDbName(dbName);
        }
        try {
            SqliteManager.getInstance(daoConfig);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
        return false;
    }

    /**
     * orm表映射
     * 根据前端数据模型,以key为表字段,建立数据表
     *
     * @param params 插件参数对象,其包装web前端数据模型的JSON字符串
     *               该数据模型结构示例{"tableName":"user","id":"","name":"","age":"","phone":""}
     *               其中tableName为固定key,如果有主键,id为固定标识主键的key;name,age,phone,为表的字段
     *
     * @return true:成功;false失败
     */
    public boolean createTable(PluginParams params) {
        String model = params.getArguments()[0];
        Context context = params.getWebView().getContext();
        try {
            SqliteManager.getInstance(context).createTable(new JSONObject(model));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
        return false;
    }

    /**
     * 通过key为tableName,value为表字段描述的JSON对象创建一系列表
     * 该方式相较与orm数据模型建表,优势在于可以未所有的列加上类型、约束等
     *
     * 每一个key-value对,对应一张表
     * 格式如:{"user":"id int primary key not null,name varchar(16) not null,age integer,address_id int",
     *         "account":"name,password",
     *         "address":"id,pid,name"}
     *         表示创建user,account,address 三表 其中,key对应的value可以带上列的约束,也可以不带
     *
     * @param params 插件参数对象,其包装web前端建表的描述JSON对象字符串
     */
    public boolean createWithTablesJSON(PluginParams params) {
        Context context = params.getWebView().getContext();
        String tablesJson = params.getArguments()[0];
        try {
            SqliteManager.getInstance(context).createTablesWithJSON(new JSONObject(tablesJson));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据数据表名称删除表
     *
     * @param params 插件参数对象,其包装要删除的表的名称
     *
     * @return true:成功;false失败
     */
    public boolean dropTable(PluginParams params) {
        String tableName = params.getArguments()[0];
        Context context = params.getWebView().getContext();
        try {
            SqliteManager.getInstance(context).dropTable(tableName);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
        return false;
    }

    /**
     * 根据数据表名称清空表数据
     *
     * @param params 插件参数对象,其包装要删除的表的名称
     * @return true:成功;false失败
     */
    public boolean clearTable(PluginParams params) {
        String tableName = params.getArguments()[0];
        Context context = params.getWebView().getContext();
        try {
            SqliteManager.getInstance(context).clearTable(tableName);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
        return false;
    }

    /**
     * orm插入表记录
     *
     * @param params 插件参数对象,其包装web前端数据模型的JSON字符串
     *               该数据模型结构示例{"tableName":"user","id":"","name":"","age":"","phone":""}
     *               其中tableName为固定key,如果有主键,id为固定标识主键的key;name,age,phone,为表的字段
     * @return true:成功;false失败
     */
    public boolean insert(PluginParams params) {
        String model = params.getArguments()[0];
        Context context = params.getWebView().getContext();
        try {
            SqliteManager.getInstance(context).insert(new JSONObject(model));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
        return false;
    }

    /**
     * 保存表记录
     * 如果存在记录,则更新,否则新增;
     * 备注:数据表中是以"id"列(数据模型中的id属性)作为记录唯一标识
     *
     * @param params 插件参数对象,其包装web前端数据模型的JSON字符串
     *               该数据模型结构示例{"tableName":"user","id":"","name":"","age":"","phone":""}
     *               其中tableName为固定key,如果有主键,id为固定标识主键的key;name,age,phone,为表的字段
     *
     * @return true:成功;false失败
     */
    public boolean save(PluginParams params) {
        String model = params.getArguments()[0];
        Context context = params.getWebView().getContext();
        try {
            SqliteManager.getInstance(context).save(new JSONObject(model));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
        return false;
    }

    /**
     * orm删除表记录
     *
     * @param params 插件参数对象,其包装web前端数据模型的JSON字符串
     *               该数据模型结构示例{"tableName":"user","id":"","name":"","age":"","phone":""}
     *               其中tableName为固定key,如果有主键,id为固定标识主键的key;name,age,phone,为表的字段
     * @return true:成功;false失败
     */
    public boolean delete(PluginParams params) {
        String model = params.getArguments()[0];
        Context context = params.getWebView().getContext();
        try {
            SqliteManager.getInstance(context).delete(new JSONObject(model));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
        return false;
    }

    /**
     * orm删除表记录
     *
     * @param params 插件参数对象,其包装web前端数据模型的JSON字符串
     *               该数据模型结构示例{"tableName":"user","id":"","name":"","age":"","phone":""}
     *               其中tableName为固定key,如果有主键,id为固定标识主键的key;name,age,phone,为表的字段
     *
     * @return true:成功;false失败
     */
    public boolean update(PluginParams params) {
        String model = params.getArguments()[0];
        Context context = params.getWebView().getContext();
        try {
            SqliteManager.getInstance(context).update(new JSONObject(model));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
        return false;
    }

    /**
     * orm表查询
     *
     * @param params 插件参数对象,其包装web前端数据模型的JSON字符串
     *               该数据模型结构示例{"tableName":"user","id":"","name":"","age":"","phone":""}
     *               其中tableName为固定key,如果有主键,id为固定标识主键的key;name,age,phone,为表的字段

     * @return 查询结果集合的JSON数组字符串。如果无结果,则返回的是"[]";
     */
    public String query(PluginParams params)  {
        String model = params.getArguments()[0];
        Context context = params.getWebView().getContext();
        JSONArray result = new JSONArray();
        try {
            result = SqliteManager.getInstance(context).query(new JSONObject(model));
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
        return result.toString();
    }

    /**
     * 通过原生SQL语句查询数据记录
     * 查询该有返回结果集(与更新,删除,新增的执行结果不一样)
     *
     * @param params 插件参数对象,其包装了可执行的查询SQL语句
     * @return 查询结果集合的JSON数组字符串。如果无结果,则返回的是"[]"
     */
    public String queryWithSql(PluginParams params) {
        Context context = params.getWebView().getContext();
        String sql = params.getArguments()[0];
        JSONArray result = new JSONArray();
        try {
            result = SqliteManager.getInstance(context).query(sql);
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
        return result.toString();
    }

    /**
     * 执行原生SQL语句
     *
     * @param params 插件参数对象,其包装了可执行的查询SQL语句
     *
     * @return true:成功;false失败
     */
    public boolean execSql(PluginParams params) {
        Context context = params.getWebView().getContext();
        String sql = params.getArguments()[0];
        try {
            SqliteManager.getInstance(context).execSQL(sql);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
        return false;
    }

    /**
     * 获取表的最大id
     *
     * @param params 配置了sql语句的插件参数对象
     * @return id
     */
    public int getTableMaxId(PluginParams params){
        Context context = params.getWebView().getContext();
        String tableName = params.getArguments()[0];
        try {
            return SqliteManager.getInstance(context).getTableMaxId(tableName);
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
        return -1;
    }
}
