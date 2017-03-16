var exec = require('cordova/exec');

exports.getLocation = function(arg0, success, error) {
    exec(success, error, "GeoLocationPlugin", "getLocation", [arg0]);
};

exports.checkPermission = function(arg0, success, error){
	exec(success, error, "GeoLocationPlugin", "checkPermission", [arg0]);
};

exports.startService = function(arg0, success, error){
	exec(success, error, "GeoLocationPlugin", "startService", [arg0]);
};

exports.stopService = function(arg0, success, error){
	exec(success, error, "GeoLocationPlugin", "stopService", [arg0]);
};

exports.showDB = function(arg0, success, error){
	exec(success, error, "GeoLocationPlugin", "showDB", [arg0]);
};

exports.config = function(config, success, error){
	var baseInterval = (config.baseInterval>=0)?config.baseInterval:5000,
	    baseFastInterval = (config.baseFastInterval>=0)?config.baseFastInterval:2000,
	    batteryThresholds = (config.batteryThresholds.length>=0)?config.batteryThresholds:[10],
	    batteryThrottles = (config.batteryThrottles.length>=1)?config.batteryThrottles:[100, 0];
	    speedThresholds = (config.speedThresholds.length>=1)?config.speedThresholds:[10],
	    speedThrottles = (config.speedThrottles.length>=1)?config.speedThrottles:[100, 0];
	exec(success, error, "GeoLocationPlugin", "configure", [baseInterval, baseFastInterval, batteryThresholds, batteryThrottles, speedThresholds, speedThrottles]);
};