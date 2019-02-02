package com.emin.digit.mobile.android.meris.framework.database.sqlite;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.emin.digit.mobile.android.meris.framework.database.DaoConfig;
import com.emin.digit.mobile.android.meris.framework.database.SqlInfo;
import com.emin.digit.mobile.android.meris.framework.database.exception.IllegalPrimaryKeyException;
import com.emin.digit.mobile.android.meris.framework.database.util.WebModelUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by Samson on 16/7/25.
 *
 * 数据库管理器,在获取数据库管理实例(单例)的同时,就根据相关的配置创建/打开数据库
 * 调用者在做数据库操作的时候,只需要调用相应的增删改查的接口,而不用手动去管理数据库本身
 * 备注:
 * 1.如果未指定数据库(通过数据库名称),则默认会获取/创建 DaoConfig 中指定的默认名称
 * 2.如果未指定创建的路劲,则默认会创建在 /data/data/app package name/databases/ 目录下
 *   例如 /data/data/com.emin.digit.mobile.android.meris/databases/
 */
public class SqliteManager {

    // TODO: 2017/11/8 关于在执行SQL出现异常和ORM的JSON异常的抛出问题
    private static final String TAG = SqliteManager.class.getSimpleName();
    private static SqliteManager instance = new SqliteManager();
    private static SqliteDatabase database;

    private SqliteManager() {
    }

    /**
     * 获取数据库管理器的实例(单例）
     * 并通过数据库配置对象初始化数据库,创建或打开,以便调用者专注数据库中数据的操作。
     * DaoConfig对象不会对DatabaseManager(单例)产生影响,而只影响配置所对应的数据库实例.
     * 因为Database的具体实现,如SqliteDatabase,(通过daoMap)具有管理多个物理数据库的能力
     *
     * @param config 数据库配置对象
     * @return 数据库管理器的单例
     */
    public static SqliteManager getInstance(DaoConfig config) {
        database = createOrOpenDatabase(config);
        return instance;
    }

    public static SqliteManager getInstance(String dbName) {
        DaoConfig config = new DaoConfig();
        config.setDbName(dbName);
        database = createOrOpenDatabase(config);
        return instance;
    }

    /**
     * 获取数据库管理器的实例(单例）
     * 并通过Android 上下文context获取默认的数据库实例
     *
     * @param context 上下文
     * @return 数据库实例
     */
    public static SqliteManager getInstance(Context context) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        database = createOrOpenDatabase(config);
        return instance;
    }

    // - - - - - - - - - - - - - 数据库的创建/打开 - - - - - - - - - - - - -
    /**
     * 通过数据库配置对象创建或打开数据库
     * 目前以SqliteDatabase作为具体的实现
     *
     * @param config 数据库配置对象
     * @return 数据库实例
     */
    private static SqliteDatabase createOrOpenDatabase(DaoConfig config) {
        return SqliteDatabase.create(config);
    }

    // 支持web前端ORM操作API
    public JSONArray query(JSONObject dataModel) throws Exception {
        return database.query(SQLHelper.buildOrmQuerySql(dataModel));
    }

    public JSONArray query(String sql) throws Exception {
        return database.query(sql);
    }

    public void insert(JSONObject dataModel) throws Exception {
        database.execSQL(SQLHelper.buildOrmInsertSql(dataModel));
    }

    public void update(JSONObject dataModel) throws Exception {
        database.execSQL(SQLHelper.buildOrmUpdateSql(dataModel));
    }

    public void save(JSONObject dataModel) throws Exception {
        if(recordExists(dataModel)) {
            update(dataModel);
            return;
        }
        insert(dataModel);
    }
    public void delete(JSONObject dataModel) throws Exception {
        database.execSQL(SQLHelper.buildOrmDeleteSql(dataModel));
    }

    public boolean execSQL(String sql) {
        Log.d(TAG,"==== execSQL:" + sql);
        try {
            database.execSQL(sql);
            return true;
        }catch (Exception e) {
            Log.e(TAG,"Exception occurred" + e.getMessage());
        }
        return false;
    }

    public void createTable(JSONObject dataModel) throws Exception {
        database.execSQL(SQLHelper.buildOrmTableSql(dataModel));
    }

    /**
     * 新建数据表
     * JSON对象中的每一个Key-value键值对都应当是一个以key为表名,value为字段列表定义的json字符串
     * 每一个key-value对,对应一张表
     *  格式如:{"user":"id int primary key not null,name varchar(16) not null,age integer,address_id int",
     *         "account":"name,password",
     *         "address":"id,pid,name"}
     *         表示创建user,account,address 三表 其中,key对应的value可以带上列的约束,也可以不带
     *
     * @param jsonObject
     */
    public void createTablesWithJSON(JSONObject jsonObject) throws Exception {
        if(jsonObject == null) { return; }
        Iterator<String> iterator =  jsonObject.keys();
        while (iterator.hasNext()){
            String tableName = iterator.next();
            String columnsDef = jsonObject.optString(tableName);
            SqlInfo sqlInfo = SQLHelper.buildTableSql(tableName,columnsDef);
            database.execSQL(sqlInfo.getSqlString());
        }
    }

    /**
     * 删除表
     *
     * @param tableName 表名
     * @throws Exception
     */
    public void dropTable(String tableName) throws Exception {
        database.execSQL(SQLHelper.buildDropTableSql(tableName));
    }

    public void clearTable(String tableName) throws Exception {
        database.execSQL(SQLHelper.buildClearTableSql(tableName));
    }

    /**
     * 更新表结构
     * 通常为新增表列
     *
     * @param dataModel 数据模型(JSON对象)
     * @throws Exception
     */
    public void alterTable(JSONObject dataModel) throws Exception {
        String tableName = WebModelUtil.checkModelMappedTable(dataModel);
        Iterator<String> iterator = dataModel.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            // 表名,跳过
            if(key.equalsIgnoreCase(WebModelUtil.MODEL_MAPPED_TABLE_KEY)) {
                continue;
            }
            if(!database.columnExistInTable(tableName, key)) {
                database.execSQL(SQLHelper.buildAddColumnSql(tableName, key));
            }
        }
    }

    /**
     * 根据前端模型匹配数据库表中是否存在相应的记录
     *
     * @param dataModel 模型
     * @return true:存在;false:不存在
     * @throws Exception 异常
     */
    public boolean recordExists(JSONObject dataModel) throws Exception {
        String tableName = WebModelUtil.checkModelMappedTable(dataModel);
        String primaryKey = WebModelUtil.MODEL_PRIMARY_KEY;
        if(!dataModel.has(primaryKey)) {
            throw new IllegalPrimaryKeyException("Primary key 'id' not found in data model " + tableName);
        }
        String primaryKeyValue = dataModel.optString(primaryKey);
        if(TextUtils.isEmpty(primaryKeyValue)) {
            return false;
        }
        return recordExists(tableName, primaryKey, primaryKeyValue);
    }

    /**
     * 验证数据库表中是否存在某行
     *
     * @param tableName 数据表名称
     * @param primaryKey 主键id
     * @param primaryKeyValue 主键的值
     * @return true:存在;false:不存在
     * @throws Exception 异常
     */
    public boolean recordExists(@NonNull String tableName,
                                @NonNull String primaryKey,
                                @NonNull String primaryKeyValue) throws Exception {
        String sql = new StringBuilder().append("SELECT ").append(primaryKey)
                .append("  FROM ").append(tableName)
                .append(" WHERE ").append(primaryKey)
                .append(" = ").append(primaryKeyValue).append(";")
                .toString();
        JSONArray result = database.query(sql);
        return (result != null && result.length() > 0);
    }

    /**
     * 获取表的最大id值
     *
     * @param tableName 表的名称
     * @return 表的最大id值
     * @throws Exception 异常
     */
    public int getTableMaxId(String tableName) throws Exception {
        return getTableMaxId(tableName, WebModelUtil.MODEL_PRIMARY_KEY);
    }

    /**
     * 获取表的最大id值
     *
     * @param tableName  表的名称
     * @param primaryKey 主键的名称
     * @return 表的最大id值
     * @throws Exception 异常
     */
    public int getTableMaxId(String tableName, String primaryKey) throws Exception {
        String sql = "SELECT MAX(" + primaryKey + ") AS id FROM " + tableName;
        JSONArray result = database.query(sql);
        if(result != null && result.length() > 0) {
            JSONObject record = result.optJSONObject(0);
            if(record.has(primaryKey)) {
                String MaxIdStr = record.optString(primaryKey);
                return Integer.parseInt(MaxIdStr);
            }
        }
        return 0;
    }
}
