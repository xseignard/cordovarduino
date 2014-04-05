## Cordovarduino

Cordovarduino is a Cordova/Phonegap plugin that enable you to use serial communication from an Android device to a serial over USB capable one.

It's a **work in progress** : Android to Arduino works, Arduino to Android now works.


## Change log
2014.04: [Derek K](https://github.com/etx)
         Implemented registerReadCallback for evented reading and Android onPause/onResume
         
2014.03: Ed. Lafargue
         Implemented read(). The success callback returns a Javascript ArrayBuffer which is the best way to handle binary data
         in Javascript. It is straightforward to convert this to a string if required - a utility function could be implemented in this plugin.

2013.11: Xavier Seignard
	First implementation

### Install it
From the root folder of your cordova project, run :
```
cordova plugin add https://github.com/stereolux/cordovarduino.git
cp plugins/org.stereolux.cordova.serial/lib/usbseriallibrary.jar platforms/android/libs
```

### How to use it
Thanks to [usb-serial-for-android](https://github.com/mik3y/usb-serial-for-android) library, you can communicate with CDC, FTDI, Arduino and other devices. Here is the Cordova plugin API.

Because you're polite, first request the permission to use the serial port to the system:
```js
serial.requestPermission(function success(), function error());
```
You can now open the serial port:
```js
serial.open(opts, function success(), function error());
```
`opts` is a JSON object with the following properties:

- baudRate: defaults to 9600
- dataBits: defaults to 8
- stopBits: defaults to 1
- parity: defaults to 0

You're now able to read and write:
```js
serial.write(data, function success(), function error());
serial.read(function success(buffer), function error());
```
`data` is the string representation to be written to the serial port.
`buffer` is a JavaScript ArrayBuffer containing the data that was just read.

Register a callback that will be invoked when the driver reads incoming data from your serial device. The success callback function will recieve an ArrayBuffer filled with the data read from serial:
```js
serial.registerReadCallback(
	function success(data){
		var view = new Uint8Array(data);
		console.log(view);
	},
	function error(){
		new Error("Failed to register read callback");
	});
```



And finally close the port:
```js
serial.close(function success(), function error())
```

### Example

A callback-ish example.

```js
var errorCallback = function(message) {
    alert('Error: ' + message);
};```

serial.requestPermission(
	function(successMessage) {
    	serial.open(
        	{baudRate: 9600},
            function(successMessage) {
        		serial.write(
                	'1',
                    function(successMessage) {
                    	alert(successMessage);
                    },
                    errorCallback
        		);
        	},
        	errorCallback
    	);
    },
    errorCallback
);
```
