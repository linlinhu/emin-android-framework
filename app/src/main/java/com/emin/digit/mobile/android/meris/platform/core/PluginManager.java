package com.emin.digit.mobile.android.meris.platform.core;

import android.util.Log;
import android.webkit.WebView;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * 插件管理器,插件的配置与执行
 * 1.给js提供插件的名字:如pluginDb,pluginFileIo,该管理器映射的是插件类的包名
 *   Web前端不再关注具体的类名
 * 2.该管理器在应用程序加载的时候,初始化
 *
 * Created by Samson on 2016/10/13.
 *
 * 变更履历
 * 2017/1/11 Webview插件追加后初始化
 * 2017/1/11 网路插件追加后初始化
 *
 */
public class PluginManager {

    private static final String TAG = PluginManager.class.getSimpleName();

    // - - - - - - - - - - 插件名与插件类的关联配置,JS需要传入插件名 START - - - - - - - - - - - - -
    // 数据库插件
    public static final String sPluginDBName = "pluginDb";
    public static final String sPluginDBClass = "com.emin.digit.mobile.android.meris.platform.plugin.PluginDatabase";

    // 文件io插件
    public static final String sPluginFileIOName = "pluginFileIo";
    public static final String sPluginFileIOClass = "com.emin.digit.mobile.android.meris.platform.plugin.PluginFileIO";

    // 资源下载插件
    public static final String sPluginDownloaderName = "pluginDownloader";
    public static final String sPluginDownloaderClass = "com.emin.digit.mobile.android.meris.platform.plugin.PluginDownloader";

    // 更新功能插件
    public static final String sPluginUpdaterName = "pluginUpdater";
    public static final String sPluginUpdaterClass = "com.emin.digit.mobile.android.meris.platform.plugin.PluginUpdater";

    // Job功能插件
    public static final String sPluginJobName = "pluginJob";
    public static final String sPluginJobClass = "com.emin.digit.mobile.android.meris.platform.plugin.PluginJob";

    // Timer插件
    public static final String sPluginTimerName = "pluginTimer";
    public static final String sPluginTimerClass = "com.emin.digit.mobile.android.meris.platform.plugin.PluginTimer";

    // Camera插件
    public static final String sPluginCameraName = "pluginCamera";
    public static final String sPluginCameraClass = "com.emin.digit.mobile.android.meris.platform.plugin.PluginCamera";

    // Camera插件
    public static final String sPluginBarcodeName = "pluginBarcode";
    public static final String sPluginBarcodeClass = "com.emin.digit.mobile.android.meris.platform.plugin.PluginBarcode";

    // Webview插件 2017/1/11
    public static final String sPluginWebviewName = "pluginWebview";
    public static final String sPluginWebviewClass = "com.emin.digit.mobile.android.meris.platform.plugin.PluginWebview";

    // 网络插件
    public static final String sPluginNetworkName = "pluginNetwork";
    public static final String sPluginNetworkClass = "com.emin.digit.mobile.android.meris.platform.plugin.PluginNetwork";

    // OkHttp service请求插件
    public static final String sPluginOkHttpName = "pluginOkHttpRequest";
    public static final String sPluginOkHttpClass = "com.emin.digit.mobile.android.meris.platform.plugin.PluginOkHttpRequest";

    // ORM
    public static final String sPluginOrmName = "pluginOrm";
    public static final String sPluginOrmClass = "com.emin.digit.mobile.android.meris.platform.plugin.PluginOrm";

    // 权限管理插件
    public static final String sPluginPermissionName = "pluginPermission";
    public static final String sPluginPermissionClass = "com.emin.digit.mobile.android.meris.platform.plugin.PluginPermission";

    // 定位插件
    public static final String sPluginLocationName = "pluginLocation";
    public static final String sPluginLocationClass = "com.emin.digit.mobile.android.meris.platform.plugin.PluginLocation";

    // 系统运行时插件
    public static final String sPluginRuntimeName = "pluginRuntime";
    public static final String sPluginRuntimeClass = "com.emin.digit.mobile.android.meris.platform.plugin.PluginRuntime";


    // - - - - - - - - - - 插件名与插件类的关联配置,JS需要传入插件名 END - - - - - - - - - - - - -
    private static HashMap<String,String> pluginMap = new HashMap<>();
    private WebView mContext;

    // TODO: 2017/1/20 getInstance的参数问题
    // singleton
    private static PluginManager instance = new PluginManager();
    public static PluginManager getInstance(WebView context){
        instance.mContext = context;
        return instance;
    }

    private PluginManager(){
    }

    // 配置,插件名称与class关联,插件名提供给JS
    public static void setup() {
        if(pluginMap == null) {
            pluginMap = new HashMap<>();
        }
        pluginMap.put(sPluginDBName, sPluginDBClass);
        pluginMap.put(sPluginFileIOName, sPluginFileIOClass);
        pluginMap.put(sPluginDownloaderName, sPluginDownloaderClass);
        pluginMap.put(sPluginUpdaterName, sPluginUpdaterClass);
        pluginMap.put(sPluginJobName, sPluginJobClass);
        pluginMap.put(sPluginTimerName, sPluginTimerClass);
        pluginMap.put(sPluginCameraName, sPluginCameraClass);
        pluginMap.put(sPluginBarcodeName, sPluginBarcodeClass);
        pluginMap.put(sPluginWebviewName, sPluginWebviewClass);  // Webview插件 2017/1/11
        pluginMap.put(sPluginNetworkName, sPluginNetworkClass);  // 网络插件 2017/1/11
        pluginMap.put(sPluginOkHttpName, sPluginOkHttpClass);  // OkHttp service请求插件 2017/10/25
        pluginMap.put(sPluginOrmName, sPluginOrmClass);       // 支持前端SQL和ORM操作数据库插件 2017/10/31
        pluginMap.put(sPluginPermissionName, sPluginPermissionClass); // app权限管理插件
        pluginMap.put(sPluginLocationName, sPluginLocationClass); // app定位管理插件
        pluginMap.put(sPluginRuntimeName, sPluginRuntimeClass); // app运行时插件
    }

    /**
     * 通过反射机制,执行插件方法
     * 通过pluginName找到JAVA类,并执行method
     *
     * @param pluginName 插件名称
     * @param methodName 执行的方法名
     * @param args 传入的参数数组(如:js中传入[arg1,arg2...])
     * @return String 结果
     */
    public String execSyncPlugin(String pluginName, String methodName, String[] args) {
        // TODO: 2017/11/15 关于返回值问题,比如true/false,前端不该受到的是"true"/"false"
        Object result;
        try {
            // 2016/10/13改善:采用pluginName与pluginClass对应的管理方式,js不再传递具体的类的全名
            //Class pluginClass = Class.forName(pluginName);
            String className = pluginMap.get(pluginName);
            Class pluginClass = Class.forName(className);
            Method method = pluginClass.getDeclaredMethod(methodName, PluginParams.class);
            PluginParams params = new PluginParams();
            params.setWebView(mContext);
            if(args != null) {
                params.setArguments(args);
            }
            result = method.invoke(pluginClass.newInstance(), params);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return (result == null) ? null : result.toString();
    }
}
