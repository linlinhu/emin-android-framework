
var synModel;

mui.back = function() {
	EminBridge.back();
}

common.getItem('btnSync').addEventListener('tap', function() {
	willSyncTemplate();
});

common.getItem('btnSubmit').addEventListener('tap', function() {
	willSubmitData();
	
});

common.getItem('btnRefresh').addEventListener('tap', function() {
	refresh();
});

// 刷新模板界面
function refresh() {
	var model = new Template();
	var queyRel = JSON.parse(EminBridge.orm.search(model));
	if(queyRel.length > 0) {
		var record = queyRel[0];
		renderWeb(JSON.parse(record.data));
	}	
}

function renderWeb(data) {
//	console.log('==== renderWeb data:' + JSON.stringify(data));
	var html = template('test', data);
	document.getElementById('content').innerHTML = html;
}

// 同步模板数据
function willSyncTemplate() {
	// 获取同步接口
	synModel = new SyncRecord();
	synModel.code = 100;
	var result = EminBridge.orm.search(synModel);
	console.log('==== result:' + result);
	var queyRel = JSON.parse(result);
	if(queyRel.length > 0) {
		var record = queyRel[0];
		synModel.id = record.id;
		synModel.lastSyncTime = record.lastSyncTime
		syncData(record);
	}
}

function syncData(syncOptions) {
	console.log('===== syncData:' + JSON.stringify(syncOptions));
//	var baseUrl = 'http://192.168.0.223:8205/';
//	var path = 'dataModel/1510797596751/getUpdates';

	var baseUrl = syncOptions.url + syncOptions.lastSyncTime + '/'; 
	var path = syncOptions.path;
	EminBridge.ohr.send({
		url:baseUrl,
		path:path,
		type:'get',
		data:{},
		success:function(result) {
			didSyncSuccess(result);
		},
		error:function() {
			mui.alert('error');
		}
	});
}

// 同步成功回调
function didSyncSuccess(result) {
	console.log('==== onSyncSuccess result:\n' + JSON.stringify(result));
	var lastSyncTime = synModel.lastSyncTime;
	var tpls = result.result;
	for(var i = 0 ; i < tpls.length ; i++) {
		var tpl = tpls[i];
		var model = new Template();
		model.id = tpl.id;
		var status = tpl.status;
		if(status == -1) {
			EminBridge.orm.remove(model);
			continue;
		}
		
		model.status = status;
		model.lastModifyTime = tpl.lastModifyTime;
		model.data = JSON.stringify(tpl);
		EminBridge.orm.save(model);
		if(model.lastModifyTime > lastSyncTime) {
			lastSyncTime = model.lastModifyTime;
		} 
//		lastSyncTime = (model.lastModifyTime > lastSyncTime) ? model.lastModifyTime : lastSyncTime
	}
	updateSynTime(lastSyncTime);
}

// 更新同步记录时间
function updateSynTime(timestamp) {
	synModel.lastSyncTime = timestamp;
	EminBridge.orm.update(synModel);
}

function willSubmitData() {
	var logData = {};
	var items = document.querySelectorAll(".formItem");
	for(var i = 0; i < items.length; i++) {
		var item = items.item(i);
		logData[item.id] = item.value;
	}
	
	var data = {
		data:JSON.stringify(logData)
	};
	console.log('==== submitData:' + JSON.stringify(data));
	submitLogData(data);
}

function submitLogData(data) {
	var baseUrl = 'http://192.168.0.223:8205/logdata/2/';
	var token = 'jxY+i3gK/VTzGTGRZshJwJYBTBU=';
	var path = 'submit';
	EminBridge.ohr.request({
		url:baseUrl,
		path:path,
		type:'post',
		header:{
			authToken:token
		},
		data:data,
		success:function(result) {
			console.log('=== onSubmitSuccess:' + JSON.stringify(result));
			if(result.success) {
				mui.alert('提交成功', '提示');
			} else {
				mui.alert(result.code, '提交失败');
			}
		},
		error:function() {
			mui.alert('error');
		}
	});
}
