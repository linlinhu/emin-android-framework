;(function(w) {
	var pluginName = "pluginOrm";
	var orm = {
		createDatabase : function(dbName) {
			var method = 'createDatabase';
			return EminBridge.executePlugin(pluginName, method,[dbName]);
		},
		add : function(entity) {
			var method = 'insert';
			return EminBridge.executePlugin(pluginName, method,[JSON.stringify(entity)]);
		},
		remove : function(entity) {
			var method = 'delete';
			return EminBridge.executePlugin(pluginName, method,[JSON.stringify(entity)]);
		},
		update : function(entity) {
			var method = 'update';
			return EminBridge.executePlugin(pluginName, method,[JSON.stringify(entity)]);
		},
		save : function(entity) {
			var method = 'save';
			return EminBridge.executePlugin(pluginName, method,[JSON.stringify(entity)]);
		},
		search : function(entity) {
			var method = 'query';
			return EminBridge.executePlugin(pluginName, method,[JSON.stringify(entity)]);
		},
		createTable : function(entity) {
			var method = 'createTable';
			return EminBridge.executePlugin(pluginName, method,[JSON.stringify(entity)]);
		},
		dropTable : function(tableName) {
			var method = 'dropTable';
			return EminBridge.executePlugin(pluginName, method,[tableName]);
		},
		clearTable : function() {
			var method = 'clearTable';
			return EminBridge.executePlugin(pluginName, method,[tableName]);
		},
		execSql : function(sql) {
			var method = 'execSql';
			return EminBridge.executePlugin(pluginName, method,[sql]);
		},
		query : function(sql) {
			var method = 'queryWithSql';
			return EminBridge.executePlugin(pluginName, method,[sql]);
		}
	};
	w.EminBridge.orm = orm;
}(window));
