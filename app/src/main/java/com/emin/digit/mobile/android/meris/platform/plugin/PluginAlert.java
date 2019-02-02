package com.emin.digit.mobile.android.meris.platform.plugin;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Samson on 16/8/4.
 */
public class PluginAlert {

//    Context mContext;

    // 弹出一个自动隐藏的提示框
    public void toast(Context context, String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
