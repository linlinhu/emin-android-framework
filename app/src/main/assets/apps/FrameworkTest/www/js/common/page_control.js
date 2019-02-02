
var pager = function() {
	
	var action = {
		openWindow : function(options) {
			EminBridge.openWindow(JSON.stringify(options));
		},
		toLoginPage : function(ops) {
			var defaultOption = {
				url: 'html/login.html',
				id: 'login'
			};
			var options = (ops == null) ? defaultOption : ops;
			EminBridge.openWindow(JSON.stringify(options));
		},
		toIndexPage : function(ops) {
			var defaultOption = {
				url: 'html/index.html',
				id: 'index'
			};
			var options = (ops == null) ? defaultOption : ops;
			EminBridge.openWindow(JSON.stringify(options));
		}
	};
	
	return action;
}();
