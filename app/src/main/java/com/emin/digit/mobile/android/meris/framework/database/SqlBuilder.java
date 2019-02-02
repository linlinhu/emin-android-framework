package com.emin.digit.mobile.android.meris.framework.database;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Samson on 16/7/25.
 *
 * 数据库可执行sql语句的构造器
 * 
 */
public class SqlBuilder {

    private static final String TAG = SqlBuilder.class.getSimpleName();

    // * * * * * * * * * * Table Level: Build Create SQL * * * * * * * * * * //
    public static SqlInfo buildCreateTableSql(String tableName , String columnsDef){

//        String className = Thread.currentThread().getStackTrace()[1].getClassName();
//        DebugLog.methodStart(className);

        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("create table if not exists ");
        sqlBuffer.append(tableName);
        sqlBuffer.append("( ");
        sqlBuffer.append(columnsDef);
        sqlBuffer.append(" )");

        SqlInfo sqlInfo = new SqlInfo();
        sqlInfo.setSqlString(sqlBuffer.toString());
        return sqlInfo;
    }

    /**
     * 构建删除所有数据表的sql
     */
    public static SqlInfo buildDropAllTablesSql() {
        String sqlStr = "select name from sqlite_master where type ='table' and name != 'sqlite_sequence'";
        SqlInfo sqlInfo = new SqlInfo(sqlStr);
        return sqlInfo;
    }

    /**
     * 构建删除指定的一组数据表的sql
     *
     * @param tableArray 一组表的JSON数组
     * @return ArrayList<SqlInfo>
     * @throws JSONException
     */
    public static ArrayList<SqlInfo> buildDropTableSqlWithJsonArray(JSONArray tableArray) throws JSONException {
        ArrayList<SqlInfo> sqlInfoList = new ArrayList<SqlInfo>();
        for(int i = 0; i < tableArray.length(); i++){
            String tableName = tableArray.getString(i);
            SqlInfo sqlInfo = buildDropTableSqlWithTableName(tableName);
            sqlInfoList.add(sqlInfo);
        }
        return sqlInfoList;
    }

    /**
     * 构建删除指定的一个数据表的sql
     *
     * @param tableName 表名
     * @return
     */
    public static SqlInfo buildDropTableSqlWithTableName(String tableName){
        if(tableName.trim().isEmpty() || tableName == null) {
//            throw new DatabaseException("Table name can not be null");
            Log.d(TAG," - - - - -Table name can not be null");
            return null;
        }
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("drop table if exists ");
        sqlBuffer.append(tableName);
        SqlInfo sqlInfo = new SqlInfo(sqlBuffer.toString());
        return sqlInfo;
    }

    // 获取ALTER SQL中的共通部分 "ALTER TABLE "
    private static String getAlterTableSql(String tableName){
        return "ALTER TABLE " + tableName;
    }

    /**
     * 构建更新数据表的sql(新增字段)
     * 备注:返回的sql语句的数组,在执行的时候需要检查表的列是否存在.或者处理已存在的列的异常情况
     *
     * @param jsonObject
     * @return
     * @throws JSONException JSONException org.json.JSONException异常
     */
    public static ArrayList<SqlInfo> buildAlterTableSql(JSONObject jsonObject) throws JSONException {
//        ALTER TABLE USER ADD COLUMN ADDRESS VARCHAR(20)
//        JSONObject alterObj = new JSONObject(jsonString);

        ArrayList<SqlInfo> sqlInfos = new ArrayList<>();

        Iterator tableKeyIterator = jsonObject.keys();
        while (tableKeyIterator.hasNext()){
            String tableName = (String)tableKeyIterator.next();
            Object columnObj = jsonObject.opt(tableName);

            // 新增多列的情况,得构造多条ALTER语句
            String columnString = columnObj.toString();
            String[] columns = columnString.split(",");
            for(int i=0;i < columns.length;i++){
                StringBuffer sqlBuffer = new StringBuffer();

                sqlBuffer.append(getAlterTableSql(tableName));
                sqlBuffer.append(" add column ");
                sqlBuffer.append(columns[i].trim());
                sqlInfos.add(new SqlInfo(sqlBuffer.toString()));
            }
        }
        return sqlInfos;
    }

    /**
     * 构建在表中新增一列
     * 备注:Sqlite目前不支持一条sql语句新增多个列
     *
     * @param tableName
     * @param columnName
     * @return
     */
    public static SqlInfo buildAddColumnSql(String tableName, String columnName){
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append(getAlterTableSql(tableName));
        sqlBuffer.append(" add column ");
        sqlBuffer.append(columnName.trim());
        return new SqlInfo(sqlBuffer.toString());
    }

    // - - - - - - - - 记录级别Sql的构建 - - - - - - - - - -

    /**
     * 构建新增表记录的sql
     *
     * @param tableName 数据表的名称字符串
     * @param recordObject 一行数据的JSON对象
     * @return
     */
    public static SqlInfo buildInsertSqlForTable(String tableName, JSONObject recordObject){
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("insert into ");
        sqlBuffer.append(tableName);
        sqlBuffer.append(" (");

        StringBuffer columnStr = new StringBuffer();
        StringBuffer valueStr = new StringBuffer();
        Iterator it = recordObject.keys();
        while (it.hasNext()){
            String key = (String)it.next();
            String value = recordObject.optString(key);
            columnStr.append(key + ",");
            valueStr.append("'" + value + "'" + ",");
        }
        columnStr.deleteCharAt(columnStr.length() - 1);
        valueStr.deleteCharAt(valueStr.length() - 1);

        // TODO: 16/7/27 生成的SQL在处理数据库表字段是数字类型的时候,是否不必转成字符串的形式,对数据库记录取值有无影响?
//        insert into staff (staff_id,first_name,last_name,address_id,store_id,username) values(2,'Sam','Hillyer',3,1,'Sam');
        sqlBuffer.append(columnStr);
        sqlBuffer.append(") values (");
        sqlBuffer.append(valueStr);
        sqlBuffer.append(")");

        return new SqlInfo(sqlBuffer.toString());
    }

    /**
     * 构建删除数据表(一个或多个)的记录sql
     *
     * @param jsonObject
     * @return
     * @throws JSONException JSONException org.json.JSONException异常
     */
    public static ArrayList<SqlInfo> buildDeleteSql(JSONObject jsonObject) throws JSONException {
        ArrayList<SqlInfo> sqlInfoList = new ArrayList<SqlInfo>();
        Iterator tableKeyIterator = jsonObject.keys();
        while (tableKeyIterator.hasNext()){
            SqlInfo sqlInfo;
            String tableName = (String)tableKeyIterator.next();
            Object object = jsonObject.opt(tableName);
            if(object instanceof JSONObject){
                JSONObject whereObj = jsonObject.optJSONObject(tableName);
                sqlInfo = buildDeleteSqlForTable(tableName,whereObj);
            }else{
                sqlInfo = buildDeleteSqlForTable(tableName,null);
            }
            sqlInfoList.add(sqlInfo);
        }
        return sqlInfoList;
    }

    /**
     * 构建删除指定数据表的记录
     *
     * @param tableName 指定的数据表名字符串
     * @param whereJson 删除的条件JSON对象
     * @return
     */
    public static SqlInfo buildDeleteSqlForTable(String tableName, JSONObject whereJson){
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("delete from ");
        sqlBuffer.append(tableName);

//        DELETE from user where name='Kate' and age='1' and ;
        if(whereJson != null && whereJson.length() != 0){
            sqlBuffer.append(" where ");
            Iterator whereKeyIterator = whereJson.keys();
            while (whereKeyIterator.hasNext()){
                String column = (String) whereKeyIterator.next();
                sqlBuffer.append(column);
                sqlBuffer.append("='");
                String value = whereJson.optString(column);
                sqlBuffer.append(value);
                sqlBuffer.append("'");

                sqlBuffer.append(" and ");
            }
            sqlBuffer.delete(sqlBuffer.length() - 5, sqlBuffer.length()-1);
        }
        SqlInfo sqlInfo = new SqlInfo(sqlBuffer.toString());
        return  sqlInfo;
    }

    public static ArrayList<SqlInfo> buildUpdateSql(JSONObject jsonObject) throws JSONException {
        ArrayList<SqlInfo> sqlinfoList = new ArrayList<>();
        Iterator<String> tableKeyItr = jsonObject.keys();
        while (tableKeyItr.hasNext()){
            String tableName = tableKeyItr.next();
            JSONObject value = jsonObject.optJSONObject(tableName);
            if(value == null){
                break;
            }

            JSONObject setJson,whereJson;
            try{
                setJson = value.getJSONObject("set");
            }catch (JSONException e){
                throw new JSONException("无法获取构建update sql中的set部分JSON");
            }

            // 采用optJSONObject(), 如果获取不到
            whereJson = value.optJSONObject("where");

            SqlInfo sqlInfo = buildUpdateSqlForTable(tableName,setJson,whereJson);
            sqlinfoList.add(sqlInfo);
        }

        return sqlinfoList;
    }

    /**
     * 更新数据表记录
     *
     * @param tableName 指定的数据表名称字符串
     * @param setJson   UPDATE SQL 中的 SET 部分JSON对象
     * @param whereJson UPDATE SQL 中的 WHERE  部分JSON对象
     * @return SqlInfo
     * @throws JSONException JSONException org.json.JSONException异常
     */
    public static SqlInfo buildUpdateSqlForTable(String tableName,
                                                 JSONObject setJson,
                                                 JSONObject whereJson) throws JSONException {

        // UPDATE TBL_USER SET PASSWORD = '12345' WHERE USER_NAME = 'COCO' AND AGE = 20;
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("update ");
        sqlBuffer.append(tableName);
        sqlBuffer.append(" ");

        // 拼接 SET 部分
        sqlBuffer.append(" set ");
        String setFragment = getColumnEqualValueString(setJson);
        sqlBuffer.append(" " + setFragment);

        // 拼接 WHERE 部分
        sqlBuffer.append(" where ");
        String whereFragment = getColumnEqualValueString(whereJson);
        sqlBuffer.append(" " + whereFragment);

        SqlInfo sqlInfo = new SqlInfo(sqlBuffer.toString());
        return  sqlInfo;
    }

    // 构建 JSON对象中key = value 字符串
    private static String getColumnEqualValueString(JSONObject jsonObject) throws JSONException {
        StringBuffer strBuffer = new StringBuffer();
        Iterator<String> columnKeyItr = jsonObject.keys();
        while (columnKeyItr.hasNext()){
            String columnName = columnKeyItr.next();
            strBuffer.append(columnName);
            strBuffer.append(" = ");
            Object columnValue = jsonObject.get(columnName);
            strBuffer.append(columnValue.toString());
        }
        return strBuffer.toString();
    }


    /**
     * 构建查询sql
     *
     * @param jsonObject
     *  示例:
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
     * @return
     * @throws JSONException
     */
    public static SqlInfo buildQuerySql(JSONObject jsonObject) throws JSONException {
        StringBuffer sqlBuffer = new StringBuffer();
        SqlInfo sqlInfo = null;
        Iterator<String> tableKeyItr = jsonObject.keys();
        if(tableKeyItr.hasNext()){
            String tableName = tableKeyItr.next();
            sqlBuffer.append("select ");

            JSONObject valueObj = jsonObject.getJSONObject(tableName);
            // SELECT 部分,如果无SELECT的JSON,则全表查询
            JSONArray selectArray = valueObj.optJSONArray("select");
            if(selectArray != null){
                for(int i = 0; i < selectArray.length(); i++){
                    String value = selectArray.getString(i);
                    sqlBuffer.append(value);
                    sqlBuffer.append(",");
                }
                // 删除最后一个逗号","
                sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
            }else {
                sqlBuffer.append("*");
            }

            // FROM 部分
            sqlBuffer.append(" from ");
            sqlBuffer.append(tableName);

            // 拼接WHERE 部分,如果无WHERE的JSON,则表示无条件查询
            JSONObject whereJson = valueObj.optJSONObject("where");
            if(whereJson != null){
                sqlBuffer.append(" where ");
                String whereString = getColumnEqualValueString(whereJson);
                sqlBuffer.append(whereString);
            }else{
            }

            // 拼接分页部分 Limit 10 Offset 10 （跳过10行(如一页10行),取10行),即获取第二页的数据
            JSONObject pageJson = valueObj.optJSONObject("page");
            if(pageJson != null){
                int pageNum = pageJson.optInt("pageNum");
                int sizePerPage = pageJson.optInt("sizePerPage");

                sqlBuffer.append(" limit ");
                sqlBuffer.append(sizePerPage);
                sqlBuffer.append(" offset ");
                sqlBuffer.append((pageNum -1) * sizePerPage);
            }else{
            }
            sqlInfo = new SqlInfo(sqlBuffer.toString());
        }
        return sqlInfo;
    }



}
