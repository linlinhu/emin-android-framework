package com.emin.digit.mobile.android.meris.platform.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Created by Samson on 2017/1/23.
 */
public class DialogUtil {

    private static ProgressDialog progressDialog;
    public static void showWaiting(Context context, String title){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(context);
            Window wd= progressDialog.getWindow();
            WindowManager.LayoutParams lp = wd.getAttributes();
            lp.alpha = 0.8f;
            wd.setAttributes(lp);
            wd.setGravity(Gravity.CENTER);
        }

        progressDialog.setTitle(title);
        progressDialog.show();
    }

    public static void closeWaiting(){
        if(progressDialog == null) return;
        progressDialog.dismiss();
    }

    public static void toast(Context context, String text){
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
