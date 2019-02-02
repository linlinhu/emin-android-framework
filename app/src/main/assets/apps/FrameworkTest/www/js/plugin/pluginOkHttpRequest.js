;(function(w){
    var pluginName = "pluginOkHttpRequest";
    var plugin = {
    	request : function(options) {
    		console.log('=== plugin ok request options:' + JSON.stringify(options));
    		var url = options.url;
    		var path = options.path;
    		var data = options.data;
    		var okCb = options.success;
    		var errorCb = options.error;
    		
    		var success; //= typeof(okCb) == 'function' ? okCb.toString() : okCb;
    		if(typeof(okCb) == 'function') {
    			success = okCb.toString();
    			console.log('=== success 匿名函数形式回调:' + success);
    		} else {
    			success = okCb;
    			console.log('=== success 显式函数形式回调:' + success);
    		}
    		
    		var fail;
    		if(typeof(errorCb) == 'function') {
    			fail = errorCb.toString();
    			console.log('=== error 匿名函数形式回调:' + success);
    		} else {
    			fail = errorCb;
    			console.log('=== error 匿名函数形式回调:' + success);
    		}
    		
    		var type = options.type;
    		var methodName = '';
    		if(type === 'post') {
    			methodName = 'post';
    			var header = options.header;
    			return EminBridge.executePlugin(pluginName,methodName,[url, path, JSON.stringify(header), JSON.stringify(data), success, fail]);
    		} 
    		methodName = 'get';
      		return EminBridge.executePlugin(pluginName,methodName,[url,path,JSON.stringify(data), okCb, errorCb]);
    	},
        cancel : function(requestId) {
            var methodName = "cancel";
            return EminBridge.executePlugin(pluginName,methodName,[requestId]);
        }
    };
    w.EminBridge.ohr = plugin;
})(window);