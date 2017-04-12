# cordova-plugin-android-background-geolocation
Cordova Plugin for Android(Background)

### Introduction
This plugin is for checking and tracking the geolocation coordinates for the android devices, which in turn efficiently utilizes the battery of the device, hence results in lower battery consumption. It does all the work in the background and hence does not blocks the UI thread.

#### Idea

The idea behind this plugin is that when you are stationary for a certain period of time the location tracking should stop which in turn lessens the burden on device's battery and whenever the person starts moving, the location updates start again.

### Techniques used

**Fused Location API**: This google API is used to track the location/movement of a person over geological area.

**Accelerometer**: This sensor is present in almost all Android devices and hence is used to track the device movement in order to make it certain that the device is moving.

### Setup

* Make sure you have Google Play Services and Google Repository installed via your android-sdk manager prior to building your application with this plugin. More information can be found here: http://developer.android.com/sdk/installing/adding-packages.html.

### Installation

Cordova:
````
cordova plugin add https://github.com/shbmbhrdwj/cordova-plugin-android-background-geolocation.git --save
````

Alternatively you can add the plugin by first downloading the zip and unzipping and then adding it to the project folder.
````
cordova plugin add <path-to-plugin-directory>
````

### How to use

The plugin exports an object at

````javascript
geolocation
````
In order for the plugin to work you will have to ask for the location permission, which can be done by:

````javascript
geolocation.checkPermission('', function(){}, function(){});
````

For getting the current location:
````javascript
geolocation.getLocation('', function(){}, function(){});
````

For starting the background geolocation tracking service:

````javascript
geolocation.startService('', function(){}, function(){});
/*NOTE: If the stopService is not called in your project then the service will run 
in background indefinitely until the phone is switched off */
````

For stopping the background geolocation tracking service:

````javascript
geolocation.stopService('', function(){}, function(){});
````

The plugin also comes with the feature of showing the Database in the app(if needed).

````javascript
geolocation.showDB('', function(){}, function(){});
````

Before starting the background service there are some default configurations that are needed to be set.

````javascript
geolocation.config({
        baseInterval:5000,
        baseFastInterval:2000,
        batteryThresholds: [10, 20],
        batteryThrottles: [100, 20, 0],
        speedThresholds: [10, 20],
        speedThrottles: [100, 20, 0]
    },
    function(){},
    function(){}
);
````
#### Configurations:
* **baseInterval:** The interval at which the location updates are desired to be accepted.
* **baseFastInterval:** The fastest interval at which the location updates are desired to be accepted.
* **batteryThresholds:** The thresholds for device's battery percentage at which the intervals should be changed.
* **batteryThrottles:** The throttle values(in percent, of base-intervals) for battery through which the interval values should be changed.
* **speedThresholds:** The thresholds for device's movement speed at which the intervals should be changed.
* **speedThrottles:** The throttle values(in percent, of base-intervals) for speed through which the interval values should be changed.

### Know Issues

Currently, the accelerometer for devices is not able to detect in car movement(working on it), working perfectly for motorbikes.

