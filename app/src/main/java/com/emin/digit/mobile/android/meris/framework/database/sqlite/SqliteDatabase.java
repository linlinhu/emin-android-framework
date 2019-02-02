package com.emin.digit.mobile.android.meris.framework.database.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.emin.digit.mobile.android.meris.framework.database.DaoConfig;
import com.emin.digit.mobile.android.meris.framework.database.Database;
import com.emin.digit.mobile.android.meris.framework.database.SqlInfo;
import com.emin.digit.mobile.android.meris.framework.database.exception.DatabaseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Samson on 16/7/22.
 * 变更履历
 * 2016/10/18 更新表的结构的时候,判断列名是否存在;一次只能新增一个列的情况下,需要构建多条alter语句
 *
 *
 * SqliteDatabase是对Android的SQLiteDatabase的封装
 * daoMap,以key-value的方式储存了数据库名称与SqliteDatabase的对应关系,
 * 即,SqliteDatabase可以管理多个SQLite数据库实例
 */
public class SqliteDatabase extends Database {

    private static final String TAG = SqliteDatabase.class.getSimpleName();

    // SqliteDatabase Map,通过数据库的名称来获取数据库实例
    private static HashMap<String,SqliteDatabase> daoMap = new HashMap<>();

    private SQLiteDatabase sqliteDb;

    // 数据库配置对象
    private DaoConfig daoConfig;

    // 私有构造方法
    private SqliteDatabase(DaoConfig config) {
        daoConfig = config;
        String dbDir = config.getTargetDirectory();
        String dbName = config.getDbName();

        if (!TextUtils.isEmpty(dbDir)) {
            sqliteDb = createDbFileOnSDCard(dbDir, dbName);
        } else {
            Context context = config.getContext().getApplicationContext();
            sqliteDb = new SqliteDbHelper(context, dbName, config.getDbVersion()).getWritableDatabase();
        }
    }

    public DaoConfig getDaoConfig() {
        return daoConfig;
    }

    /**
     * 通过context上下文创建数据库,因为数据库为设置,所以采用DaoConfig中默认的数据库名称
     *
     * @param context 上下文
     * @return SqliteDatabase单例(相应数据库名称单例,不同的数据库名称对应不同的数据库单例)
     */
    public static SqliteDatabase create(Context context){
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        return create(config);
    }

    /**
     * 通过上下文和数据库名称创建数据库
     *
     * @param context 上下文
     * @param dbName 数据库名称
     * @return SqliteDatabase单例(相应数据库名称单例,不同的数据库名称对应不同的数据库单例)
     */
    public static SqliteDatabase create(Context context, String dbName){
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setDbName(dbName);
        return create(config);
    }

    /**
     * 创建Db
     *
     * @param context 上下文
     * @param dbName 数据库名称
     */
    public static SqliteDatabase create(Context context, String targetDirectory, String dbName) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setDbName(dbName);
        config.setTargetDirectory(targetDirectory);
        return create(config);
    }

    /**
     * 通过数据库配置对象创建数据库
     *
     * @param config 数据库配置对象
     * @return SqliteDatabase单例(相应数据库名称单例,不同的数据库名称对应不同的数据库单例)
     */
    public static SqliteDatabase create(DaoConfig config){
        return getInstance(config);
    }

    // 避免create()重载的函数调用产生的线程安全问题
    private synchronized static SqliteDatabase getInstance(DaoConfig config) {
        SqliteDatabase sqliteDb = daoMap.get(config.getDbName());
        if(sqliteDb == null) {
            sqliteDb = new SqliteDatabase(config);
            daoMap.put(config.getDbName(),sqliteDb);
        }
        return sqliteDb;
    }

    public void close() {
        sqliteDb.close();
    }

    /**
     * 在SD卡的指定目录上创建文件
     *
     * @param sdcardPath sd卡路劲
     * @param dbFileName 数据库文件名称
     * @return SQLiteDatabase实例
     */
    private SQLiteDatabase createDbFileOnSDCard(String sdcardPath, String dbFileName) {
        File file = new File(sdcardPath, dbFileName);
        if(file.exists()) {
            return SQLiteDatabase.openOrCreateDatabase(file, null);
        }
        try {
            if (file.createNewFile()) {
                return SQLiteDatabase.openOrCreateDatabase(file, null);
            }
        } catch (IOException e) {
            throw new DatabaseException("数据库文件创建失败", e);
        }
        return null;
    }

    public JSONArray query(@NonNull SqlInfo sql) throws Exception {
        return query(sql.getSqlString());
    }

    public JSONArray query(@NonNull String sql) throws Exception {
        Cursor cursor = rawQuery(sql);
        JSONArray recordArray = new JSONArray();
        int index = 0;
        while (cursor.moveToNext()) {
            JSONObject record = convertCursorToJSON(cursor);
            recordArray.put(index, record);
            index++;
        }
        if(!cursor.isClosed()) {
            cursor.close();
        }
        return recordArray;
    }

    public void execSQL(@NonNull SqlInfo sql) throws Exception {
        execSQL(sql.getSqlString());
    }

    public void execSQL(@NonNull String sql) throws Exception {
        Log.d(TAG,"### execSQL:" + sql);
        try {
            this.sqliteDb.execSQL(sql);
        } catch (Exception e) {
            throw new DatabaseException("执行SQL异常");
        }
    }

    /**
     * 判断某个列是否已存在表中
     * sqlite的系统表sqlite_master 记录了数据库中的表、索引等
     * 其中字段'sql'记录了创建表的sql语句,所以如果存在某个字段,那么建表的sql语句一定包含了该字段的名字
     *
     * @param tableName 表名
     * @param columnName 字段名
     * @return true:字段已存在 false:字段不存在
     */
    public boolean columnExistInTable(String tableName, String columnName) {
        Cursor cursor = null ;
        try {
            String sql = "SELECT * FROM SQLITE_MASTER WHERE NAME = ? AND SQL LIKE ?";
            cursor = sqliteDb.rawQuery(sql, new String[]{tableName , "%" + columnName + "%"});
            return (null != cursor && cursor.moveToFirst());
        } catch (Exception e) {
            Log.e(TAG,"check Column Exists in table error:" + e.getMessage()) ;
        } finally {
            if(null != cursor && !cursor.isClosed()){
                cursor.close() ;
            }
        }
        return false ;
    }

    private Cursor rawQuery(@NonNull String sql) {
        return sqliteDb.rawQuery(sql, null);
    }

    // 将Cursor转换成JSONObject
    private JSONObject convertCursorToJSON(@NonNull Cursor cursor) throws JSONException {
        JSONObject json = new JSONObject();
        for(int i = 0; i < cursor.getColumnCount(); i++) {
            String columnName = cursor.getColumnName(i);
            Object value;
            int valueType = cursor.getType(i);
            switch (valueType) {
                case Cursor.FIELD_TYPE_INTEGER:
                    value = cursor.getLong(i);
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    value = cursor.getFloat(i);
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    value = cursor.getString(i);
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    value = cursor.getBlob(i);
                    break;
                case Cursor.FIELD_TYPE_NULL:
                    value = "";
                    break;
                default:
                    value = "";
                    break;
            }
            json.put(columnName, value);
        }
        return json;
    }

    // 封装Android的SQLiteOpenHelper,负责创建/打开数据库
    class SqliteDbHelper extends SQLiteOpenHelper {
        //带全部参数的构造函数，此构造函数必不可少
        public SqliteDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        //带三个参数的构造函数，调用的是带所有参数的构造函数
        public SqliteDbHelper(Context context, String name, int version){
            this(context, name, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
