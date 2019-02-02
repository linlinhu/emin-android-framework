package com.emin.digit.mobile.android.meris.framework.database.util;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.emin.digit.mobile.android.meris.framework.database.exception.UnknownTableException;

import org.json.JSONObject;

/**
 * author: Samson
 * created on: 2017/11/2 0002 下午 6:12
 * description:
 */
public class WebModelUtil {

    public static final String MODEL_MAPPED_TABLE_KEY = "tableName";

    public static final String MODEL_PRIMARY_KEY = "id";

    /**
     * 检查数据模型所映射的表是否已经指定
     *
     * @param dataModel 数据模型
     * @return 表名
     */
    public static String checkModelMappedTable(JSONObject dataModel) {
        String tableName = dataModel.optString(MODEL_MAPPED_TABLE_KEY);
        if (TextUtils.isEmpty(tableName)) {
            throw new UnknownTableException("数据模型中未指明确切的数据表");
        }
        return tableName;
    }

    /**
     * 检查数据模型的属性值是否为空
     * 包括'null'这样的字符串
     *
     * @param obj 值
     * @return true:是空值;false:非空值
     */
    public static boolean isNullValue(Object obj) {
        if(obj == null) {
            return true;
        }
        String value = obj.toString();
        if(TextUtils.isEmpty(value) ||  value.trim().equalsIgnoreCase("null")) {
            return true;
        }
        return false;
    }

    /**
     * 检查是否数据模型的主键
     * 约定主键为id
     *
     * @param key 数据模型的属性名称,如"id"
     * @return true:是主键;false:非主键
     */
    public static boolean isTablePrimaryKey(String key) {
        if(isNullValue(key)) return false;
        if(key.equalsIgnoreCase(MODEL_PRIMARY_KEY)) {
            return true;
        }
        return false;
    }

    /**
     * 检查数据模型的属性名称是否为数据表的描述,如tableName
     * 备注:数据模型必须包含一个属性为tableName的key,以描述该数据模型对应哪一张表;
     * 所以该属性并不是表中的列
     *
     * @param key 数据模型中的key
     * @return true:是;false:否
     */
    public static boolean isTableNameKey(String key) {
        if(isNullValue(key)) return false;
        if(key.equalsIgnoreCase(MODEL_MAPPED_TABLE_KEY)) {
            return true;
        }
        return false;
    }
}
