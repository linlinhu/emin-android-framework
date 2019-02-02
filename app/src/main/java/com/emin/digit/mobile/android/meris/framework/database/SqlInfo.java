package com.emin.digit.mobile.android.meris.framework.database;

/**
 * Created by Samson on 16/7/25.
 */
public class SqlInfo {

    /**
     * 原生SQL语句字符串
     */
    private String sqlString;

    public SqlInfo(){

    }

    /**
     * 构造方法
     *
     * @param sqlString sql语句
     */
    public SqlInfo(String sqlString){
        this.sqlString = sqlString;
    }

    public String getSqlString() {
        return sqlString;
    }

    public void setSqlString(String sqlString) {
        this.sqlString = sqlString;
    }

}
