package com.emin.digit.mobile.android.meris.framework.database;

import android.content.Context;
import android.util.Log;


import com.emin.digit.mobile.android.meris.framework.database.sqlite.SQLHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Samson on 16/7/25.
 *
 * 数据库管理器,在获取数据库管理实例(单例)的同时,就根据相关的配置创建/打开数据库
 * 调用者在做数据库操作的时候,只需要调用相应的增删改查的接口,而不用手动去管理数据库本身
 */
public class DatabaseManager {

    private static final String TAG = DatabaseManager.class.getSimpleName();
    private static DatabaseManager instance = new DatabaseManager();
    private static SqliteDatabase database;

    /**
     *
     * 获取数据库管理器的实例(单例）
     * 并通过数据库配置对象初始化数据库,创建或打开,以便调用者专注数据库中数据的操作。
     * DaoConfig对象不会对DatabaseManager(单例)产生影响,而只影响配置所对应的数据库实例.
     * 因为Database的具体实现,如SqliteDatabase,(通过daoMap)具有管理多个物理数据库的能力
     *
     * @param config 数据库配置对象
     * @return 数据库管理器的单例
     */
    public static DatabaseManager getInstance(DaoConfig config){
        database = createOrOpenDatabase(config);
        return instance;
    }

    /**
     * 获取数据库管理器的实例(单例）
     * 并通过Android 上下文context获取默认的数据库实例
     *
     * @param context
     * @return
     */
    public static DatabaseManager getInstance(Context context){
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        database = createOrOpenDatabase(config);
        return instance;
    }

    // 私有构造方法,避免外部自行创建管理器实例
    private DatabaseManager(){}

    // - - - - - - - - - - - - - 数据库的创建/打开 - - - - - - - - - - - - -
    /**
     * 通过数据库配置对象创建或打开数据库
     * 目前以SqliteDatabase作为具体的实现
     *
     * @param config 数据库配置对象
     * @return
     */
    private static SqliteDatabase createOrOpenDatabase(DaoConfig config){
        SqliteDatabase sqliteDb = SqliteDatabase.create(config);
        return sqliteDb;
    }

    // - - - - - - - - - - - - - 数据库表级别操作 - - - - - - - - - - - - -

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
    public void createTable(JSONObject jsonObject) throws JSONException{
        database.createTable(jsonObject);
    }

    /**
     * 删除数据表
     *
     * @param jsonArray 要删除的数据表的JSON数组
     * @throws JSONException org.json.JSONException异常
     */
    public void dropTables(JSONArray jsonArray) throws JSONException{
        database.dropTables(jsonArray);
    }

    /**
     * 更新表的结构
     *
     * @param jsonObject 更新表的JSON对象
     *                       {"TBL_USER":{
     * "SET":{"PASSWORD":"12345"},
     *          "WHERE":{"USER_NAME":"COCO","AGE":20 }
     *       }
     *  }
     *
     * @throws JSONException org.json.JSONException异常
     */
    public void updateTable(JSONObject jsonObject) throws JSONException{
        database.updateTable(jsonObject);
    }

    // - - - - - - - - - - - - - 数据库记录级别操作 - - - - - - - - - - - - -

    /**
     * 新增数据库记录
     *
     * @param jsonObject
     * @throws JSONException
     */
    public void insert(JSONObject jsonObject) throws Exception{
        database.insert(jsonObject);
    }

    /**
     * 新增数据库记录
     *
     * @param tableName
     * @param array
     * @throws JSONException
     */
    public void insert(String tableName, JSONArray array) throws Exception {
        database.insertRecordsIntoTable(tableName,array);
    }

    /**
     * 删除一个或多个数据表记录
     *
     * @param jsonObject 删除表记录的JSON对象
     * 格式:
     * key为数据表的名称,value为删除数据的条件的JSON对象字符串
     *        格式形如:
     *        {"TABLE1":{"ID":100},
     *         "TABLE2":{"ID":2,"NAME":"ABC"}
     *         "TABLE3":{}}
     *
     *         删除TABLE1中ID为100的数据；
     *         删除TABLE2中ID为2且NAME为ABC的数据;
     *         删除TABLE3所有的数据
     *
     * @throws JSONException org.json.JSONException异常
     */
    public void delete(JSONObject jsonObject) throws JSONException{
        database.delete(jsonObject);
    }

    /**
     * 更新数据表记录
     *
     * @param jsonObject
     * @throws JSONException
     */
    public void update(JSONObject jsonObject) throws JSONException {
        database.update(jsonObject);
    }

    /**
     * 查询数据表记录
     *
     * @param jsonObject
     *      *  示例:
     *     { "t_user":{
     *          {"select":["user_id","age","name"]},
     *          {"where":{"age":20}},
     *          {"page":{"pageNum":3,"sizePerPage":10}}
     *       }
     *     }
     *
     *  构建的SQL:select user_id,age,name from t_user where age=20 limit 10 offset 20
     *  分页部分：第3页,每页10行,即最多10条，跳过前面两页的20条
     *
     * @return 记录的JSON数组
     * @throws JSONException org.json.JSONException异常
     */
    public JSONArray query(JSONObject jsonObject) throws JSONException{
        JSONArray result = database.query(jsonObject);
        return result;
    }

    public JSONArray queryWithSqlString(String sqlString) throws JSONException{
        JSONArray result = database.queryWithSqlString(sqlString);
        return result;
    }

    /**
     * 执行原生sql语句
     *
     * @param sqlString
     */
    public boolean execSQL(String sqlString) {
        Log.d(TAG,"execSQL:" + sqlString);
        try {
            database.execSQL(sqlString);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    /**
     * 批量执行sql语句
     *
     * @param sqlArray sql语句数组
     * @throws Exception
     */
    public void execSqlBatch(ArrayList<String> sqlArray) throws Exception{
        if(sqlArray == null || sqlArray.size() == 0) return;
        database.execSqlBatch(sqlArray);
    }

    /**
     * 获取表的最大id值
     *
     * @param tableName 表的名称
     * @return
     * @throws JSONException
     */
    public int getTableMaxId(String tableName) throws JSONException {
        String sql = "select Max(id) as id from " + tableName;
        JSONArray result = queryWithSqlString(sql);
        if(result != null && result.length() > 0 && result.optJSONObject(0).has("id")){
            String MaxIdStr = result.optJSONObject(0).optString("id");
            return Integer.parseInt(MaxIdStr);
        }
        return 0;
    }
}
