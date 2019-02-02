package com.emin.digit.mobile.android.meris.framework.database;

import android.content.Context;

/**
 * 数据库访问配置类
 * 配置数据库的名称(如果不配置,则取默认值),
 *
 * Created by Samson on 16/7/22.
 */
public class DaoConfig {

    private static final String DEFAULT_DB_NAME = "meris.db";
    private static final int DEFAULT_VERSION = 1;

    /**
     * 创建数据库的上下文
     */
    private Context context;

    /**
     * 数据库类型
     */
    private String dbType;

    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * 数据库版本
     */
    private int dbVersion;

    /**
     * 数据库储存位置(SD卡,不设置则在应用的私有路劲的databases下)
     */
    private String targetDirectory;

    public DaoConfig() {
        dbName = DEFAULT_DB_NAME;
        dbVersion = DEFAULT_VERSION;
    }

    /**
     * 获取上下文
     *
     * @return context 上下文
     */
    public Context getContext() {
        return context;
    }

    /**
     * 设置上下文
     * Modified on 2017/11/7 context采用全局app上下文,db操作app任何地方都应该可以访问
     *
     * @param context 上下文
     */
    public void setContext(Context context) {
        // this.context = context;
        this.context = context.getApplicationContext();
    }

    /**
     * 获取数据库类型(目前是SQLite）
     *
     * @return
     */
    public String getDbType() {
        return dbType;
    }

    /**
     * 设置数据库类型
     *
     * @param dbType 数据库类型
     */
    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    /**
     * 获取数据库名称
     *
     * @return string 数据库名称
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * 设置数据库名称
     *
     * @param dbName 数据库名称
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * 获取数据库版本
     *
     * @return int 数据库版本
     */
    public int getDbVersion() {
        return dbVersion;
    }

    /**
     * 设置数据库版本
     *
     * @param dbVersion 数据库版本
     */
    public void setDbVersion(int dbVersion) {
        this.dbVersion = dbVersion;
    }

    /**
     * 获取数据库SD卡的路劲(创建的数据库是在SD卡的情况)
     *
     * @return string 数据库在SD卡的路劲
     */
    public String getTargetDirectory() {
        return targetDirectory;
    }

    /**
     * 设置数据库SD卡的路劲
     *
     * @param targetDirectory SD卡的路劲
     */
    public void setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }
}
