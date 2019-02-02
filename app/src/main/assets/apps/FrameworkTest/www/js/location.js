
mui.back = function() {
	EminBridge.back();
}

common.getItem('btnSingle').addEventListener('tap', function() {
	console.log('btnSingle clicked..');
	common.getItem('startTime').innerHTML = new Date().Format('yyyy/MM/dd hh:mm:ss');
	EminBridge.pluginLocation.getCurrentPosition('onSuccess', 'onError');
});

common.getItem('btnStartListen').addEventListener('tap', function() {
	console.log('btnStartListen clicked..');
	common.getItem('startTime').innerHTML = new Date().Format('yyyy/MM/dd hh:mm:ss');
//	EminBridge.pluginLocation.startListen('onSuccess', 'onError');
	EminBridge.pluginLocation.startListen(function(result) {
		alert('success:' + result);
	}, function(e) {
		alert('fail:' + e);
	});
});

common.getItem('btnStopListen').addEventListener('tap', function() {
	console.log('btnStopListen clicked..');
	common.getItem('startTime').innerHTML = new Date().Format('yyyy/MM/dd hh:mm:ss');
	EminBridge.pluginLocation.stopListen('onSuccess', 'onError');
});

function onSuccess(result) {
	console.log("locate result:" + result);
	var loc = JSON.parse(result);
	common.getItem('provider').innerHTML = loc.provider;
	common.getItem('longitude').innerHTML = loc.longitude;
	common.getItem('latitude').innerHTML = loc.latitude;
	common.getItem('accuracy').innerHTML = loc.accuracy;
	common.getItem('locatedTime').innerHTML = new Date(loc.time).Format('yyyy/MM/dd hh:mm:ss'); //loc.time;
}

function onError(e) {
	console.log("locate error:" + e.message);
}
