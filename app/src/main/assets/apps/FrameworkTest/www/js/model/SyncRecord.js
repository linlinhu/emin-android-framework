/**
 * 模板资源/数据表的数据模型
 */
function SyncRecord() {
	BaseEntity.call(this);
	this.tableName = "sync_record";
	
	this.id = null;
	this.code = null;
	this.name = null;
	this.url = null;
	this.path = null;
	this.lastSyncTime = null;
}

SyncRecord.prototype = new BaseEntity();