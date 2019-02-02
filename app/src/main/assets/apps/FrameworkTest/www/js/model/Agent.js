
/**
 * product表的数据模型
 */
function Agent() {
	BaseEntity.call(this);
	//this.tableName = "agent";
	
	this.id = null;
	this.name = null;
}

Agent.prototype = new BaseEntity();