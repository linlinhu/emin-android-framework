function BaseEntity() {
	this.tableName = "";
	
	this.description = function() {
		console.log("Entity for table '" + this.tableName + "'");
	};
}

BaseEntity.prototype.test = function() {
	alert("test");
};

