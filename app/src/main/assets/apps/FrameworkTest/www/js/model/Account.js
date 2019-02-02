/**
 * Account表的数据模型
 */
function Account() {
	BaseEntity.call(this);
	this.tableName = "account";
	
	this.userId = null;
	this.account = null;
	this.password = null;
}
Account.prototype = new BaseEntity();
