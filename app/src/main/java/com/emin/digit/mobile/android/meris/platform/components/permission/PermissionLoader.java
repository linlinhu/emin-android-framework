package com.emin.digit.mobile.android.meris.platform.components.permission;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 权限加载器
 * 负责加载权限的配置文件,该文件根据业务进行配置
 *
 * Created by Sam on 2017/10/17.
 */
public class PermissionLoader {

    private static final String TAG = PermissionLoader.class.getSimpleName();

    private static PermissionLoader instance = null;
    private Context mContext;

    public static PermissionLoader getInstance(Context context) {
        if(instance == null) {
            synchronized (PermissionLoader.class) {
                if(instance == null) {
                    instance = new PermissionLoader(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private PermissionLoader(Context context) {
        mContext = context;
    }

    /**
     * 加载权限配置文件
     *
     * @param filePath 文件路径
     */
    public String load(String filePath) {
        AssetManager manager = mContext.getAssets();
        String result = "";
        InputStream inStream = null;
        InputStreamReader inReader = null;
        BufferedReader bufReader;
        try {
            inStream = manager.open(filePath);
            inReader = new InputStreamReader(inStream);
            bufReader = new BufferedReader(inReader);
            String line = "";
            while((line = bufReader.readLine()) != null) {
                result += line;
            }
            inReader.close();
            inStream.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if(inReader != null) {
                try {
                    inReader.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }

            if(inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }
}
