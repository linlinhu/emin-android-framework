package com.emin.digit.mobile.android.meris.framework.zxing;

import android.graphics.Bitmap;
import android.os.Handler;

//import com.emin.digit.test.zxing.camera.CameraManager;
//import com.emin.digit.test.zxing.view.ViewfinderView;
import com.emin.digit.mobile.android.meris.framework.zxing.camera.CameraManager;
import com.emin.digit.mobile.android.meris.framework.zxing.view.ViewfinderView;
import com.google.zxing.Result;

/**
 * Created by Samson on 16/9/20.
 */
public interface IBarHandler {
    Handler getHandler();
    ViewfinderView getViewfinderView();
    CameraManager getCameraManager();
//    void autoFocus();
//    void handleDecode(Result obj, Bitmap barcode);
    void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor);
    void drawViewfinder();
//    boolean isRunning();
}
