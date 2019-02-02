/**
 * product表的数据模型
 */
function Product() {
	BaseEntity.call(this);
	this.tableName = "product";
	
	this.id = null;
	this.name = null;
	this.price = null;
	this.agent = null;
}

Product.prototype = new BaseEntity();