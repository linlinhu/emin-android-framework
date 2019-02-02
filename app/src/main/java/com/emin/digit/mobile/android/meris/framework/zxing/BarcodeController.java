package com.emin.digit.mobile.android.meris.framework.zxing;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.emin.digit.mobile.android.meris.R;
import com.emin.digit.mobile.android.meris.framework.zxing.android.CaptureActivityHandler;
import com.emin.digit.mobile.android.meris.framework.zxing.android.InactivityTimer;
import com.emin.digit.mobile.android.meris.framework.zxing.android.IntentSource;
import com.emin.digit.mobile.android.meris.framework.zxing.camera.CameraConfigurationUtils;
import com.emin.digit.mobile.android.meris.framework.zxing.camera.CameraManager;
import com.emin.digit.mobile.android.meris.framework.zxing.view.ViewfinderView;
import com.emin.digit.mobile.android.meris.platform.core.EMBaseActivity;
import com.emin.digit.mobile.android.meris.platform.core.EMHybridActivity;
import com.emin.digit.mobile.android.meris.platform.core.EMHybridWebView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Created by Samson on 16/9/19.
 */
public class BarcodeController implements SurfaceHolder.Callback,IBarHandler{

    private static final String TAG = BarcodeController.class.getSimpleName();

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Collection<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;

    // 声音控制
    private MediaPlayer mediaPlayer;
    private boolean playBeep;

    // 摄像头与二维码控制
    private SurfaceView surfaceView;
    private CameraManager cameraManager;
    private IntentSource source;
    private Map<DecodeHintType, ?> decodeHints;

    // 二维码区域控制
    private FrameLayout mainView;
    private Rect barcodeRect;
    EMBaseActivity activity;

    // TODO: 2016/10/9 测试
    private boolean isLoaded;
    public boolean isLoaded() {
        return isLoaded;
    }

    private boolean isPause = false;
    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    public Rect getBarcodeRect() {
        return barcodeRect;
    }

    public void setBarcodeRect(Rect barcodeRect) {
        this.barcodeRect = barcodeRect;
    }

    // - - - - - - - - IBarHandler Interface Start - - - - - - - -
    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    /**
     * 扫描成功，处理反馈信息
     *
     * @param rawResult
     * @param barcode
     * @param scaleFactor
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        boolean fromLiveScan = barcode != null;
        if (fromLiveScan) {
            playBeepSound();
            // 回传到WebView
            EMHybridWebView webView = EMHybridActivity.getWebViewList().getLast();
            String callBackName = "onBarcodeScan";
            webView.loadUrl("javascript:" + callBackName + "('" + rawResult.getText() + "')");
            Log.d(TAG,"== " + getNowDateStr() + " 扫描成功 result:" + rawResult.getText());
            handler.pausePreviewAndDecode(1500L);
        }
    }
    // - - - - - - - - IBarHandler Interface End - - - - - - - -
    private String getNowDateStr(){
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        return format.format(new Date());
    }

    private static BarcodeController ourInstance = new BarcodeController();
    public static BarcodeController getInstance() {
        return ourInstance;
    }

    private BarcodeController() {
    }

    /**
     * 在目标Activity加载二维码扫描View
     * 全屏加载(无Rect的参数)
     *
     * @param act 加载二维码扫描的Activity
     */
    public void loadBarcodeView(EMBaseActivity act){
        activity = act;
        init(null);
    }

    /**
     * 在目标Activity加载二维码扫描View,二维码区域只显示rect部分
     *
     * @param act 目标Activity
     * @param rect  二维码扫描显示rect部分
     */
    public void loadBarcodeView(EMBaseActivity act, Rect rect){
        activity = act;
        init(rect);
        this.barcodeRect = rect;
    }

    /**
     * 在目标Webview上加载二维码扫描View
     *
     * @param webView 加载二维码扫描的Webview
     * @param rect
     */
    public void loadBarcodeView(WebView webView,Rect rect) {
    }

    public void openFlashLight(Boolean on) {
        Camera camera = cameraManager.getCamera();
        if(camera == null) {
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        CameraConfigurationUtils.setTorch(parameters, on);
        camera.setParameters(parameters);
    }

    /**
     * 继续开始扫秒
     */
    public void continueScan(){
        Log.d(TAG,"## BarcodeController continueScan");
        if(handler != null){
            Log.d(TAG,"## CaptureActivityHandler is not null will restartPreviewAndDecode");
            handler.restartPreviewAndDecode();
        }else{
            Log.d(TAG,"### CaptureActivityHandler is null");
        }
    }

    /**
     * 暂停扫秒多少毫秒
     *
     * @param millis 毫秒
     */
    public void pauseScan(long millis) {
        if(handler != null){
            handler.pausePreviewAndDecode(millis);
        }
    }

    /**
     * 停止扫描
     */
    public void stopScan() {
        if(handler != null){
            handler.stopPreviewAndDecode();
        }
    }

    // 取消二维码view的加载
    public void cancel(){
        if(isLoaded){
            stop();
        }
    }

    // 隐藏二维码扫描view
    public void hideBarcodeView(){
        if(isLoaded && mainView != null){
            cameraManager.stopPreview();
            mainView.setVisibility(View.GONE);
        }
    }

    public void showBarcodeView(){
        if(isLoaded && mainView != null){
            if(mainView.getVisibility() == View.INVISIBLE || mainView.getVisibility() == View.GONE){
                mainView.setVisibility(View.VISIBLE);
                cameraManager.startPreview();
                handler.restartPreviewAndDecode();
            }
        }
    }

    private void init(Rect rect){
        if(isLoaded) {return;}

        // 二维码扫描的布局文件:xml
        LayoutInflater inflater = LayoutInflater.from(activity);
        FrameLayout layout = (FrameLayout)inflater.inflate(R.layout.barcode_capture_2,null);

        // SurfaceView(硬件摄像头的预览)
        surfaceView = (SurfaceView) layout.findViewById(R.id.preview_view_2);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();

        // viewfinderView
        viewfinderView = (ViewfinderView) layout.findViewById(R.id.viewfinder_view_2);
        cameraManager = new CameraManager(activity.getApplication());
        viewfinderView.setCameraManager(cameraManager);
        handler = null;

        // 主Activity的布局
        FrameLayout actFrameLayout = (FrameLayout) activity.findViewById(R.id.idActivityHybrid);

        // 用mainView装载SurfaceView和ViewfinderView整体
        mainView = new FrameLayout(activity.getApplicationContext());
        mainView.setClipChildren(true);
        //mainView.setClipBounds();
        //FrameLayout.LayoutParams bcLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, divHeight);
        FrameLayout.LayoutParams bcLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        bcLayoutParams.gravity = Gravity.LEFT|Gravity.TOP;
        bcLayoutParams.leftMargin   = 0;
        bcLayoutParams.topMargin    = 0;//-640;
        bcLayoutParams.bottomMargin = 0;//640;
        actFrameLayout.addView(mainView,bcLayoutParams);  // add mainView into activity
        mainView.setBackgroundColor(Color.BLUE);

        if(surfaceView!= null && surfaceView.getParent() != null) {
            layout.removeView(surfaceView);
        }

        if(viewfinderView != null && viewfinderView.getParent() != null) {
            layout.removeView(viewfinderView);
        }

        FrameLayout.LayoutParams svLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mainView.addView(surfaceView,svLp);

        FrameLayout.LayoutParams vfLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mainView.addView(viewfinderView,vfLp);

        if (hasSurface) {
            // activity在paused时但不会stopped,因此surface仍旧存在；
            // surfaceCreated()不会调用，因此在这里初始化camera
            Log.d(TAG,"== hasSurface");
            initCamera(surfaceHolder);
        } else {
            // 重置callback，等待surfaceCreated()来初始化camera
            Log.d(TAG,"== has no Surface");
            surfaceHolder.addCallback(this);
        }
        isLoaded = true;
        if(rect != null) {
            clipBoundsForView(mainView,rect);
            BarcodeViewConfig.getInstance().rect = rect;
        }
        initBarcodeSound();
    }

    private void clipBoundsForView(View view, int left, int top, int right, int bottom){
        if(mainView == null) return;
        Rect originRect = new Rect();
        view.getGlobalVisibleRect(originRect);
        Rect newRect = new Rect(left,top,right,bottom);
        clipBoundsForView(view,newRect);
    }

    private void clipBoundsForView(View view,Rect rect){
        if(view == null) return;
        view.setClipBounds(rect);
    }

    private static final float BEEP_VOLUME = 0.10f;
    private void initBarcodeSound(){
        if (mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            try {
                AssetFileDescriptor file = activity.getResources().getAssets().openFd("res/beep.ogg");
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                mediaPlayer = null;
            }
        }
    }

    private void playBeepSound() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    // When the beep has finished playing, rewind to queue up another one.
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    // - - - - - - SurfaceHolder.Callback start - - - - - -
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
    // - - - - - - SurfaceHolder.Callback End - - - - - -

    // - - - - - - Life circle - - - - - -
    public void stop() {
        Log.d(TAG,"===== stop..");
        cameraManager.stopPreview();
        hasSurface = false;
        decodeFormats = null;
        characterSet = null;
        cameraManager.closeDriver();
        if(mainView != null){
            Log.d(TAG,"=== mainView is not null will remove it.mainView:" + mainView);
            FrameLayout layout = (FrameLayout)mainView.getParent();
            if(layout != null){
                layout.removeView(mainView);
            }
        }
        isLoaded = false;
    }

    protected void onDestroy() {
    }

    /**
     * 初始化Camera
     *
     * @param surfaceHolder
     */
    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            // 打开Camera硬件设备
            cameraManager.openDriver(surfaceHolder);
            // 创建一个handler来打开预览，并抛出一个运行时异常
            if (handler == null) {
                handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
        }
    }
}
