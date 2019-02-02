/**
 * 模板资源/数据表的数据模型
 */
function Template() {
	BaseEntity.call(this);
	this.tableName = "template";
	
	this.id = null;
	this.data = null;
	this.status = null;
	this.lastModifyTime = null;
}

Template.prototype = new BaseEntity();