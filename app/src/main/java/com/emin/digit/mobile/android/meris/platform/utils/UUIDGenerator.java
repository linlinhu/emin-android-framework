package com.emin.digit.mobile.android.meris.platform.utils;

import java.util.UUID;

/**
 * author: Samson
 * created on: 2017/11/22 下午 2:05
 * description:
 * 唯一标识生成工具
 */
public class UUIDGenerator {


    public UUIDGenerator() {
    }

    /**
     * 生成唯一标识
     * Example: C2FEEEAC-CFCD-11D1-8B05-00600806D9B6
     *
     * @return UUID
     */
    public static String genUUID() {
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        // 去掉"-"符号
        String temp = str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23) + str.substring(24);
        return temp;
    }

    /**
     * 获取指定长度的UUID
     *
     * @param length 长度
     * @return 指定长度的UUID
     */
    public static String genUUID(int length) {
        // TODO: 2017/11/22 指定长度标识,截取后是否会重复的调查
        String id= genUUID();
        return id.substring(0, length);

    }

}
