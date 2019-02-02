var unitTest = function() {
	var action = {
		getTemplateData : function() {
			return getTemplateData();
		}
	};
	
	function getTemplateData() {
		var data = {
		  "success": true,
		  "result": [
		    {
		      "callBackUrl": "http://localhost:8205/logdata/testexcel",
		      "code": "DMDV",
		      "groups": [
		        {
		          "createTime": 1510292743635,
		          "modelId": 1,
		          "lastModifyTime": 1510292743635,
		          "name": "标题",
		          "index": 1,
		          "id": 1,
		          "items": [
		            {
		              "modelGroupId": 1,
		              "itemToken": "cs",
		              "reqex": "",
		              "index": 1,
		              "title": "测试",
		              "type": 1,
		              "required": true,
		              "createTime": 1510292880207,
		              "lastModifyTime": 1510292880208,
		              "id": 1,
		              "value": "",
		              "status": 1
		            }
		          ],
		          "status": 1
		        }
		      ],
		      "title": "测试",
		      "version": 2,
		      "isDefault": false,
		      "createTime": 1510292665805,
		      "lastModifyTime": 1510292880219,
		      "name": "测试",
		      "id": 1,
		      "serviceId": "dmdm",
		      "status": 1
		    }
		  ]
		}
		return data;
	}
	
	return action;
}();