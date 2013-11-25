## Cordovarduino

Cordovarduino is a Cordova/Phonegap plugin that enable you to use serial communication from an Android device to a serial over USB capable one.

### Context
This work was made during an art residency hosted at the [Stereolux, Laboratoire Arts et Technologies](http://www.stereolux.org/laboratoire-arts-et-technologies/archives) with [Coup de foudre](https://www.facebook.com/coup.defoudre.716) and [Xavier Seignard](http://drangies.fr).

The goal was to create a tablet app to control a [tesla coil](http://www.youtube.com/watch?v=X2elQ6RR7lw) with an [Arduino](http://arduino.cc). The chosen technology ([Cordova](http://cordova.io)) had no capabilities to handle such serial over usb communication.

### Install it
From the root folder of your cordova project, run :
```
cordova plugin add https://github.com/xseignard/cordovarduino
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
serial.write(function success(), function error());
serial.read(data, function success(), function error());
```
`data` is the string representation to be written to the serial port.

And finally close the port:
```js
serial.close(function success(), function error())
```

### Example

A callback-ish example.

```js
var errorCallback = function(message) {
    alert('Error: ' + message);
};

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