package com.emin.digit.mobile.android.meris.framework.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.emin.digit.mobile.android.meris.framework.database.exception.DatabaseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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

    private void debug(String info){
        System.out.println(info);
    }

    // SqliteDatabase Map,通过数据库的名称来获取数据库实例
    private static HashMap<String,SqliteDatabase> daoMap = new HashMap<String, SqliteDatabase>();

    // Android SQLiteDatabase
    private SQLiteDatabase sqliteDb;

    // 数据库配置对象
    private DaoConfig daoConfig;

    // 私有构造方法
    private SqliteDatabase(DaoConfig config){
        if(config.getDbName() == null){
            debug("Db name is null");
        }

        if (config.getTargetDirectory() != null && config.getTargetDirectory().trim().length() > 0) {
            this.sqliteDb = createDbFileOnSDCard(config.getTargetDirectory(), config.getDbName());
        }else{
            sqliteDb = new SqliteDbHelper(config.getContext().getApplicationContext(),
                    config.getDbName(), config.getDbVersion()).getWritableDatabase();
        }
        this.daoConfig = config;
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
     * 创建FinalDb
     *
     * @param context
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
    private synchronized static SqliteDatabase getInstance(DaoConfig config){
        SqliteDatabase sqliteDb = daoMap.get(config.getDbName());
        if(sqliteDb == null){
            sqliteDb = new SqliteDatabase(config);
            daoMap.put(config.getDbName(),sqliteDb);
        }
        return sqliteDb;
    }

    public void close(){
        sqliteDb.close();
    }

    /**
     * 在SD卡的指定目录上创建文件
     *
     * @param sdcardPath sd卡路劲
     * @param dbfilename 数据库文件名称
     * @return
     */
    private SQLiteDatabase createDbFileOnSDCard(String sdcardPath, String dbfilename) {
        Log.d(TAG,"will create db file on sdcard:path:" + sdcardPath + " dbfileName:" + dbfilename);
        File dbf = new File(sdcardPath, dbfilename);
        if (!dbf.exists()) {
            try {
                if (dbf.createNewFile()) {
                    return SQLiteDatabase.openOrCreateDatabase(dbf, null);
                }
            } catch (IOException ioex) {
                throw new DatabaseException("数据库文件创建失败", ioex);
            }
        } else {
            return SQLiteDatabase.openOrCreateDatabase(dbf, null);
        }
        return null;
    }

    // - - - - - - - - - - - - - 表级别操作 - - - - - - - - - - - - -

    /**
     * 创建数据表
     *
     * @param jsonObject 创建表的json数据
     */
    public void createTable(JSONObject jsonObject) {
        if(jsonObject == null) return;
        Iterator<String> iterator =  jsonObject.keys();
        while (iterator.hasNext()){
            String tableName = iterator.next();
            String columnsDef = jsonObject.optString(tableName);
            SqlInfo sqlInfo = SqlBuilder.buildCreateTableSql(tableName,columnsDef);
            sqliteDb.execSQL(sqlInfo.getSqlString());
        }
    }

    /**
     * 删除指定的一组数据表
     *
     * @param tableArray 一个/多个数据表的JSON数组
     */
    public void dropTables(JSONArray tableArray) throws JSONException {
        ArrayList<SqlInfo> sqlInfoList = SqlBuilder.buildDropTableSqlWithJsonArray(tableArray);
        execSqlInfoList(sqlInfoList);
    }

    /**
     * 删除所有数据表
     */
    public void dropAllTables() {
        SqlInfo sqlInfo = SqlBuilder.buildDropAllTablesSql();
        Cursor cursor = queryWithSqlInfo(sqlInfo);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                sqliteDb.execSQL("DROP TABLE " + cursor.getString(0));
            }
        }
        if (cursor != null) {
            cursor.close();
//            cursor = null;
        }
    }

    /**
     * 删除指定的一个数据表
     *
     * @param tableName 表名字符串
     */
    public void dropTableWithTableName(String tableName){
        SqlInfo sqlInfo = SqlBuilder.buildDropTableSqlWithTableName(tableName);
        execSqlInfo(sqlInfo);
    }

    /**
     * 更新数据表的结构
     * 示例:{"USER":"PHONE_NUM VARCHAR(20)"}
     * 表示要在数据表USER中新增一个字段较PHONE_NUM,类型为VARCHAR,长度为20;
     * 当然也可以省略类型的说明,目前SQLite不支持删除字段
     * 并且不支持在一条sql语句中新增多个列,只能一条语句添加一个字段
     *
     * @param jsonObject json格式数据
     */
    public void updateTable(JSONObject jsonObject) throws JSONException {

        ArrayList<SqlInfo> sqlList = new ArrayList<>();
        Iterator tableKeyIterator = jsonObject.keys();
        while (tableKeyIterator.hasNext()){
            String tableName = (String)tableKeyIterator.next();
            Object columnObj = jsonObject.opt(tableName);

            // 新增多列的情况,得构造多条ALTER语句,且需要判断该列名是否已经存在
            String columnString = columnObj.toString();
            String[] columns = columnString.split(",");
            for(int i=0;i < columns.length;i++){
                if(!columnIsExist(tableName, columns[i])){
                    SqlInfo sql =  SqlBuilder.buildAddColumnSql(tableName, columns[i]);
                    sqlList.add(sql);
                }else{
                    Log.d(TAG,columns[i] + " 在" + tableName + " 已经存在！");
                }
            }
        }
        this.execSqlInfoList(sqlList);
    }

    /**
     * 判断某个列是否已存在表中
     *
     * @param tableName 表名
     * @param columnName 字段名
     * @return true:字段已存在 false:字段不存在
     */
    private boolean columnIsExist(String tableName, String columnName){
        boolean result = false ;
        Cursor cursor = null ;
        try{
            cursor = sqliteDb.rawQuery( "select * from sqlite_master where name = ? and sql like ?"
                    , new String[]{tableName , "%" + columnName + "%"} );
            result = null != cursor && cursor.moveToFirst() ;
        }catch (Exception e){
            Log.e(TAG,"checkColumnExists..." + e.getMessage()) ;
        }finally{
            if(null != cursor && !cursor.isClosed()){
                cursor.close() ;
            }
        }

        return result ;

    }

    // - - - - - - - - - - - - - 表记录级别操作 - - - - - - - - - - - - -

    /**
     * 新增数据表记录,一行或多行
     * 如果插入一行,可以构建value为单独的JSON对象,也可以包装在JSON数组里；
     * 插入多行,则构建JSON数组.
     *
     * @param jsonObject 新增记录的JSON对象
     * 格式:
     *        key为数据表的名称,value为所要新增的数据记录。
     *        value可以是JSON数组(新增多行),也可以是一个单条记录的JSON对象(新增一行)
     *        每一条记录是一个单独的JSON对象,其key为列名,value为具体的值。

     */
    public void insert(JSONObject jsonObject) throws Exception{

        // table
        Iterator tableKeyItr = jsonObject.keys();
        while(tableKeyItr.hasNext()){
            String tableName = (String) tableKeyItr.next();
            Object value = jsonObject.opt(tableName);
            Log.i(TAG,"value class:" + value.getClass().toString());
            if(value instanceof JSONArray){
                Log.i(TAG,"instance of JSONArray:" + value.toString());
                insertRecordsIntoTable(tableName,(JSONArray)value);
            }
            if(value instanceof JSONObject){
                Log.i(TAG,"instance of JSONObject :" + value.toString());
                insertRecordIntoTable(tableName,(JSONObject)value);
            }
        }
    }

    /**
     * 向指定的数据表中新增若干行记录
     *
     * @param tableName 表的名称字符串
     * @param recordsArray 新增的记录的JSON数组
     * @throws JSONException org.json.JSONException异常
     */
    public void insertRecordsIntoTable(String tableName, JSONArray recordsArray) throws Exception {

        // 2016-11-22 加入通过JSON对象批量新增时的事务处理
//        for(int i = 0 ; i < recordsArray.length(); i++){
//            JSONObject recordObject = recordsArray.getJSONObject(i);
//            insertRecordIntoTable(tableName,recordObject);
//        }

        sqliteDb.beginTransaction();
        try{
            for(int i = 0 ; i < recordsArray.length(); i++){
                JSONObject recordObject = recordsArray.getJSONObject(i);
                insertRecordIntoTable(tableName,recordObject);
            }
            sqliteDb.setTransactionSuccessful();
        }catch (Exception e){
            Log.d(TAG,"批量执行sql出现异常，rollback!");
            throw e;
        }finally {
            // 结束事务
            sqliteDb.endTransaction();
            // TODO: 2016/10/18 关于数据库是否需要关闭的问题，1.每次执行完都要关闭；2.程序退出时候才关闭
            //sqliteDb.close();
        }
    }

    /**
     * 向指定的数据表中新增一行记录
     *
     * @param tableName 表的名称字符串
     * @param recordObject json格式数据
     */
    public void insertRecordIntoTable(String tableName, JSONObject recordObject) throws Exception{
        SqlInfo sqlInfo = SqlBuilder.buildInsertSqlForTable(tableName,recordObject);
        execSqlInfo(sqlInfo);
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
    public void delete(JSONObject jsonObject) throws JSONException {
        ArrayList<SqlInfo> sqlInfoList = SqlBuilder.buildDeleteSql(jsonObject);
        execSqlInfoList(sqlInfoList);
    }

    /**
     * 删除单个指定的数据表中的记录
     *
     * @param tableName 指定的表名称字符串
     * @param whereJson 删除的条件JSON对象
     * key为列的名称,value为列的值
     *        示例:
     *        如DELETE FROM TABLE_NAME WHERE COLUMN_NAME_1 = 100 AND COLUMN_NAME_2 ='ABC';
     *        参数 whereJsonString 则为 {"COLUMN_NAME_1":100,"COLUMN_NAME_2":"ABC"}
     */
    public void deleteFromTable(String tableName, JSONObject whereJson){
        SqlInfo sqlInfo = SqlBuilder.buildDeleteSqlForTable(tableName,whereJson);
        execSqlInfo(sqlInfo);
    }

    /**
     * 更新数据表记录
     *
     * @param jsonObject
     * @throws JSONException org.json.JSONException异常
     */
    public void update(JSONObject jsonObject) throws JSONException {
        ArrayList<SqlInfo> sqlInfoList = SqlBuilder.buildUpdateSql(jsonObject);
        execSqlInfoList(sqlInfoList);
    }

    // - - - - - - - - - - 表记录查询 - - - - - - - - - -
    /**
     * 查询表记录
     *
     * @param jsonObject 查询JSON对象
     * @return
     * @throws JSONException
     */
    public JSONArray query(JSONObject jsonObject) throws JSONException {
        return queryFromSingleTable(jsonObject);
    }

    /**
     * 单表查询
     *
     * @param jsonObject 查询JSON对象
     *                   示例:
     *                   { "T_USER":{
     *                           {"SELECT":["USER_ID","AGE"]},
     *                           {"WHERE":{"USER_ID":2}}
     *                        }
     *                    }
     *
     * @return JSONArray对象 从目标数据表中查询出的一组记录
     * @throws JSONException org.json.JSONException异常
     */
    public JSONArray queryFromSingleTable(JSONObject jsonObject) throws JSONException {
        SqlInfo sqlInfo = SqlBuilder.buildQuerySql(jsonObject);
        Cursor cursor = queryWithSqlInfo(sqlInfo);
        JSONArray recordArray = new JSONArray();
        int index = 0;
        while (cursor.moveToNext()){
            JSONObject recordObj = new JSONObject();
            for(int i = 0; i < cursor.getColumnCount(); i++){
                String columnName = cursor.getColumnName(i);
                String value = cursor.getString(i);
                recordObj.put(columnName,value);
            }
            recordArray.put(index,recordObj);
            index++;
        }
        if(cursor != null){
            cursor.close();
            cursor = null;
        }
        return recordArray;
    }

    /**
     * 通过原生SQL查询数据表记录
     *
     * @param sqlString
     * @return
     */
    public JSONArray queryWithSqlString(String sqlString) throws JSONException {
        SqlInfo sqlInfo = new SqlInfo(sqlString);
        Cursor cursor = queryWithSqlInfo(sqlInfo);
        JSONArray recordArray = new JSONArray();
        int index = 0;
        while (cursor.moveToNext()){
            JSONObject recordObj = getJSONObjectFromCursor(cursor);
            recordArray.put(index,recordObj);
            index++;
        }
        if(cursor != null){
            cursor.close();
            cursor = null;
        }
        return recordArray;
    }

    // 将Cursor转换成JSONObject
    private JSONObject getJSONObjectFromCursor(Cursor cursor){
        if(cursor == null) return null;
        JSONObject recordObj = new JSONObject();
        for(int i = 0; i < cursor.getColumnCount(); i++){
            String columnName = cursor.getColumnName(i);
            String value = cursor.getString(i);
            try{
                recordObj.put(columnName,value);
            }catch (JSONException e){
                recordObj = null;
            }
        }
        return recordObj;
    }

    public Cursor queryFromTable(JSONObject jsonObject) throws JSONException {
        SqlInfo sqlInfo = SqlBuilder.buildQuerySql(jsonObject);
        Cursor cursor = queryWithSqlInfo(sqlInfo);
        return cursor;
    }

    private Cursor queryWithSqlInfo(SqlInfo sqlInfo){
        return sqliteDb.rawQuery(sqlInfo.getSqlString(),null);
    }

    // - - - - - - - - - - 执行SQL - - - - - - - - - -

    /**
     *
     * 执行原生可执行的Sql语句
     *
     * @param sqlString 原生SQL语句
     */
    public void execSQL(String sqlString) throws SQLException {
        Log.d(TAG,"execSQL:" + sqlString);
        try {
            this.sqliteDb.execSQL(sqlString);
        }catch (SQLException e){
            throw e;
        }
    }

    /**
     * 执行将原生SQL语句封装的SqlInfo
     *
     * @param sqlInfo 封装了原生sql语句的SqlInfo
     */
    public void execSqlInfo(SqlInfo sqlInfo){
        execSQL(sqlInfo.getSqlString());
    }

    /**
     * Added by Sam 2016-12-01
     * 备注:与execSqlInfoList合并
     *
     * @param sqlArray
     */
    public void execSqlBatch(ArrayList<String> sqlArray) throws Exception {
        sqliteDb.beginTransaction();
        try {
            for(int i=0; i < sqlArray.size(); i++) {
                execSQL(sqlArray.get(i));
            }
            sqliteDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            sqliteDb.endTransaction();
        }
    }

    /**
     * 批量执行SQL
     *
     * @param sqlInfoList SqlInfo列表
     */
    public void execSqlInfoList(ArrayList<SqlInfo> sqlInfoList){
        if(sqlInfoList == null) {
            Log.i(TAG,"arrayList is null.return now");
            return;
        }
        sqliteDb.beginTransaction();
        try{
            for(int i = 0 ; i < sqlInfoList.size(); i++){
                execSqlInfo(sqlInfoList.get(i));
            }
            sqliteDb.setTransactionSuccessful();
        }catch (Exception e){
            Log.d(TAG,"批量执行sql出现异常，rollback!");
            e.printStackTrace();
        }finally {
            // 结束事务
            sqliteDb.endTransaction();
            //sqliteDb.close();
        }

//        List<String> sqlStrList = new ArrayList<>();
//        for(SqlInfo sql : sqlInfoList) {
//            sqlStrList.add(sql.getSql());
//        }
//        execSqlBatch(sqlStrList);
    }

    // 封装Android的SQLiteOpenHelper,负责创建/打开数据库
    class SqliteDbHelper extends SQLiteOpenHelper {

//        private DbUpdateListener mDbUpdateListener;

        //三个不同参数的构造函数
        //带全部参数的构造函数，此构造函数必不可少
        public SqliteDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        //带三个参数的构造函数，调用的是带所有参数的构造函数
        public SqliteDbHelper(Context context, String name, int version){
            this(context, name, null, version);
        }

        /*
        public SqliteDbHelper(Context context, String name, int version, DbUpdateListener dbUpdateListener) {
            super(context, name, null, version);
            DebugLog.i(TAG,"EMDatabase SqliteDbHelper constructor");
            this.mDbUpdateListener = dbUpdateListener;
        }*/

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            /*
            if (mDbUpdateListener != null) {
                mDbUpdateListener.onUpgrade(db, oldVersion, newVersion);
            } else { // 清空所有的数据信息
                dropDb();
            }*/
        }
    }



}
