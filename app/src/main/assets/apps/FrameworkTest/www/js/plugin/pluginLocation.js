/**
 * 定位插件
 * 可获取当前位置;监听位置的变化;
 * 通过回调函数形式返回定位/监听结果
 *
 * created by Sam 2017/11/08
 */
;(function(w){
    var pluginName = "pluginLocation";
    var plugin = {
        getCurrentPosition : function(okCB, errorCB) {
            var methodName = "getCurrentPosition";
            EminBridge.executePlugin(pluginName,methodName,[okCB, errorCB]);
        },
        startListen : function(okCB, errorCB) {
        	alert(okCB);
        	var methodName = "startListen";
        	var success = (typeof(okCB) != "function") ? null : function(result) {
        		okCB(result);
        	};
        	var fail = (typeof(errorCB) != "function") ? null : function(msg) {
        		errorCB(msg);
        	};
        	alert(success);
            EminBridge.executePlugin(pluginName,methodName,[success, fail]);
        },
        stopListen :function(listenId) {
        	var methodName = "stopListen";
            return EminBridge.executePlugin(pluginName,methodName,[listenId]);
        }
    };
    w.EminBridge.pluginLocation = plugin;

})(window);