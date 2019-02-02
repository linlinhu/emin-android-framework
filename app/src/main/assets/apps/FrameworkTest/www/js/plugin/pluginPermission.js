/**
 * 权限管理插件
 * 可对app是否被赋予了权限进行检查确认
 *
 * created by Sam 2017/11/07
 */
;(function(w){
    var pluginName = "pluginPermission";
    var pg = {
        check : function(name){
            var methodName = "checkWithName";
            EminBridge.executePlugin(pluginName,methodName,[name]);
        }
    };
    w.EminBridge.pluginPermission = pg;

})(window)
