/**
 * Array的扩展
 * 
 * Created by Sam 2017-08-11
 */


/**
 * 检索数组中的值
 * 
 * @param {Object} val 值
 */
Array.prototype.indexOf = function (val) {
    for (var i = 0; i < this.length; i++) {
        if (this[i] == val) {
            return i;
        }
    }
    return -1;
};

/**
 * 移除数组中的值
 * @param {Object} val
 */
Array.prototype.removeValue = function (val) {
    var index = this.indexOf(val);
    if (index > -1) {
        this.splice(index, 1);
    }
};

/* 
 *  方法:Array.remove(dx) 
 *  功能:根据元素位置值删除数组元素. 
 *  参数:元素值 
 *  返回:在原数组上修改数组 
 */  
Array.prototype.remove = function (dx) {  
    if (isNaN(dx) || dx > this.length) {
        return false;
    }
    for (var i = 0, n = 0; i < this.length; i++) {
        if (this[i] != this[dx]) {
            this[n++] = this[i];  
        }
    }
    this.length -= 1;  
};
