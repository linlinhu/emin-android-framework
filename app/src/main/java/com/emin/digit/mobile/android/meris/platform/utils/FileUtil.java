package com.emin.digit.mobile.android.meris.platform.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * author: Samson
 * created on: 2017/11/22 下午 5:03
 * description:
 */
public class FileUtil {

    /**
     * 获取assets目录下的文件的内容
     *
     * @param context android上下文
     * @param filePath 文件路径
     *                 如:apps/apps/myApp/www/config.json
     *
     * @return 返回文件的内容
     */
    public static String readAssetsFile(Context context, String filePath) {
        String Result="";
        InputStreamReader inputReader = null;
        AssetManager assetManager = null;
        try {
            assetManager = context.getResources().getAssets();
            InputStream in = assetManager.open(filePath);
            inputReader = new InputStreamReader(in);
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            while((line = bufReader.readLine()) != null) {
                Result += line;
            }
            inputReader.close();
            //assetManager.close();
            return Result;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("IOException occurred. ", e);
        }finally {
            if(inputReader != null){
                try {
                    inputReader.close();
                }catch (IOException e){
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }

            if(assetManager != null){
                //assetManager.close();

            }
        }
    }
}
