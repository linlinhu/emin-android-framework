
//document.write('<script src="../js/model/BaseEntity.js"></script>');
/**
 * User表的数据模型
 */
function User() {
	BaseEntity.call(this);
	this.tableName = "user";
	
	this.id = null;
	this.name = null;
	this.age = null;
	this.phone = null;
}

User.prototype = new BaseEntity();
//User.prototype.setId = function(id) {
//	this.id = id;
//};
//User.prototype.getId = function() {
//	return this.id;
//};
//
//User.prototype.setName = function(name) {
//	this.name = name;
//};
//User.prototype.getName = function() {
//	return this.name;
//};
//
//User.prototype.setAge = function(age) {
//	this.age = age;
//};
//User.prototype.getAge = function() {
//	return this.age;
//};
//
//User.prototype.setPhone = function(phone) {
//	this.phone = phone;
//};
//User.prototype.getPhone = function() {
//	return this.phone;
//};
