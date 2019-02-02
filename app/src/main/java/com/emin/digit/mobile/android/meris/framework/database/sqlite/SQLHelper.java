package com.emin.digit.mobile.android.meris.framework.database.sqlite;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.emin.digit.mobile.android.meris.framework.database.SqlInfo;
import com.emin.digit.mobile.android.meris.framework.database.util.WebModelUtil;

import org.json.JSONObject;

import java.util.Iterator;

/**
 * author: Samson
 * created on: 2017/11/1 0001 上午 11:17
 * description:
 * SQL语句构建器,基于前端数据模型,构建相应的可执行SQL语句
 *
 * 备注:
 * 1.由于Sqlite是弱类型数据库,其存的值不是有列的类型决定,而是由存进来的值的类型决定
 *  具体的值类型,可能得根据业务逻辑进行转换,比如年龄,存进来是以字符串统一处理,那么业务知晓其为数字型字符串,
 *  则可以在业务层面进行转换处理;
 * 2.通过前端js数据模型进来的,主键都设置为"id",且为数字类型,支持自增
 */
public class SQLHelper {

    /**
     * 构建查询(SELECT)SQL语句
     *
     * @param dataModel 数据模型
     * @return SqlInfo
     */
    public static SqlInfo buildOrmQuerySql(JSONObject dataModel) {
        String tableName = WebModelUtil.checkModelMappedTable(dataModel);
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("SELECT * FROM ").append(tableName);
        String whereString = buildWhere(dataModel);
        if(whereString.length() != 0) {
            sqlBuffer.append(" WHERE ").append(whereString);
        }
        sqlBuffer.append(";");
        Log.d("SQLHelper", "==== build query sql:" + sqlBuffer.toString());
        return new SqlInfo(sqlBuffer.toString());
    }

    /**
     * 构建删除(DELETE)SQL语句
     *
     * @param dataModel dataModel 数据模型
     * @return @See SqlInfo
     */
    public static SqlInfo buildOrmInsertSql(JSONObject dataModel) {
        String tableName = WebModelUtil.checkModelMappedTable(dataModel);
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("INSERT INTO ").append(tableName).append(" (");

        StringBuffer columnBuffer = new StringBuffer();
        StringBuffer valueBuffer = new StringBuffer();
        Iterator<String> iterator = dataModel.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            // 表名,跳过
            if(WebModelUtil.isTableNameKey(key)) {
                continue;
            }
            // 对模型的属性值进行类型匹配,主要是数字型的值,比如 column = 100 和 column = '100'的结果差异
            Object value = dataModel.opt(key);
            if(WebModelUtil.isNullValue(value)) {
                continue;
            }
            columnBuffer.append(key).append(",");
            if(value instanceof Number) {
                valueBuffer.append(value);
            } else {
                valueBuffer.append("'").append(value).append("'");
            }
            valueBuffer.append(",");
        }
        columnBuffer.deleteCharAt(columnBuffer.length() - 1);
        valueBuffer.deleteCharAt(valueBuffer.length() - 1);
        sqlBuffer.append(columnBuffer).append(") VALUES (").append(valueBuffer).append(");");
        return new SqlInfo(sqlBuffer.toString());
    }

    public static SqlInfo buildOrmUpdateSql(JSONObject dataModel) {
        String tableName = WebModelUtil.checkModelMappedTable(dataModel);
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("UPDATE ").append(tableName).append(" SET ");

        Object primaryKeyVal = "";
        Iterator<String> iterator = dataModel.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            // 表名,跳过
            if(WebModelUtil.isTableNameKey(key)) {
                continue;
            }
            // 主键,保存起来,最后将作为更新的条件
            if(WebModelUtil.isTablePrimaryKey(key)) {
                primaryKeyVal = dataModel.opt(key);
                continue;
            }
            // 对模型的属性值进行类型匹配,主要是数字型的值,比如 column = 100 和 column = '100'的结果差异
            Object value = dataModel.opt(key);
            if(WebModelUtil.isNullValue(value)) {
                continue;
            }
            if(value instanceof Number) {
                sqlBuffer.append(key).append("=").append(value);
            } else {
                sqlBuffer.append(key).append("=").append("'").append(value).append("'");
            }
            sqlBuffer.append(",");
        }
        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        sqlBuffer.append(" WHERE ").append(WebModelUtil.MODEL_PRIMARY_KEY)
                 .append("=").append(primaryKeyVal).append(";");
        return new SqlInfo(sqlBuffer.toString());
    }

    /**
     * 构建基于数据模型的删除SQL语句
     * 示例:DELETE FROM USER WHERE name='Kate' and age='1';
     *
     *
     * @param dataModel 数据模型(JSON对象)
     * @return SqlInfo
     */
    public static SqlInfo buildOrmDeleteSql(JSONObject dataModel) {
        String tableName = WebModelUtil.checkModelMappedTable(dataModel);
        StringBuffer sqlBuffer = new StringBuffer();
        String whereString = buildWhere(dataModel);

        // 处于安全考虑,确保不能因为无条件,将表清空的情况
        if(whereString.length() != 0) {
            sqlBuffer.append("DELETE FROM ").append(tableName)
                     .append(" WHERE ").append(whereString).append(";");
        }
        return new SqlInfo(sqlBuffer.toString());
    }

    /**
     * 构建基于数据模型的创建表SQL语句
     *
     * @param dataModel 数据模型(JSON对象)
     * @return SqlInfo
     */
    public static SqlInfo buildOrmTableSql(JSONObject dataModel) {
        String tableName = WebModelUtil.checkModelMappedTable(dataModel);
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append("(");

        Iterator<String> iterator = dataModel.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            // 表名,跳过
            if(WebModelUtil.isTableNameKey(key)) {
                continue;
            }
            sqlBuffer.append(key);
            // 主键id,加上sqlite主键描述;同时查询匹配id的值时,数字和字符串结果统一,如id=100和id='100'一致
            if(WebModelUtil.isTablePrimaryKey(key)) {
                sqlBuffer.append(" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ");
            }
            sqlBuffer.append(",");
        }
        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        sqlBuffer.append(")").append(";");
        return new SqlInfo(sqlBuffer.toString());
    }

    public static SqlInfo buildTableSql(String tableName, String columnsDef) {
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("CREATE TABLE IF NOT EXISTS ");
        sqlBuffer.append(tableName);
        sqlBuffer.append("( ");
        sqlBuffer.append(columnsDef);
        sqlBuffer.append(" )");
        return new SqlInfo(sqlBuffer.toString());
    }

    /**
     * 根据表名,构建删除表SQL语句
     *
     * @param tableName 表名
     * @return SqlInfo
     */
    public static SqlInfo buildDropTableSql(String tableName) {
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("DROP TABLE IF EXISTS ").append(tableName).append(";");
        return new SqlInfo(sqlBuffer.toString());
    }

    public static SqlInfo buildClearTableSql(String tableName) {
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("TRUNCATE TABLE").append(tableName).append(";");
        return new SqlInfo(sqlBuffer.toString());
    }

    /**
     * 构建在表中新增一列
     * 备注:Sqlite目前不支持一条sql语句新增多个列
     * 示例:ALTER TABLE USER ADD COLUMN MEMO
     *     ALTER TABLE USER ADD COLUMN MEMO VARCHAR(20)
     *
     * @param tableName 表名称
     * @param columnName 列名称
     * @return SqlInfo
     */
    public static SqlInfo buildAddColumnSql(@NonNull String tableName,
                                            @NonNull String columnName) {
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("ALTER TABLE ").append(tableName)
                 .append(" ADD COLUMN ").append(columnName.trim()).append(";");
        return new SqlInfo(sqlBuffer.toString());
    }

    /**
     * 根据web前端数据模型,过滤与构建SQL语句的条件部分
     * 支持模糊查询,通过value的值包含 "%" 判断是否是模糊查询
     *
     * @param dataModel 数据模型
     * @return SQL语句where条件字符串
     */
    private static String buildWhere(JSONObject dataModel) {
        StringBuffer stringBuffer = new StringBuffer();
        Iterator<String> iterator = dataModel.keys();
        while(iterator.hasNext()) {
            String key = iterator.next();
            if(WebModelUtil.isTableNameKey(key)) {
                continue;
            }

            Object obj = dataModel.opt(key);
            if(WebModelUtil.isNullValue(obj)) {
                continue;
            }
            stringBuffer.append(key);
            if(obj instanceof Number) {
                stringBuffer.append("=").append(obj);
            } else {
                if(obj.toString().contains("%")) {
                    stringBuffer.append(" LIKE ");
                } else {
                    stringBuffer.append("=");
                }
                stringBuffer.append("'").append(obj).append("'");
            }
            stringBuffer.append(" AND ");
        }
        if(stringBuffer.length() > 0) {
            stringBuffer.delete(stringBuffer.length() - 5, stringBuffer.length() - 1);
        }
        return stringBuffer.toString().trim();
    }

}
