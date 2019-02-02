function BaseEntity() {
	this.tableName = "";
	this.description = function() {
		console.log("Entity for table '" + this.tableName + "'");
	};
}

BaseEntity.prototype.test = function() {
	alert("test");
};

// user表的实体类
function User() {
	BaseEntity.call(this);
//	BaseEntity.apply(User, arguments);
	this.tableName = "user";
	this.name = null;
	this.age = null;
	this.phone;
}
User.prototype = new BaseEntity();

// account表的实体类
function Account() {
	BaseEntity.call(this);
	this.tableName = "account";
	this.id = null;
	this.userId = null;
	this.account = null;
	this.password = "";	
}
Account.prototype = new BaseEntity();
