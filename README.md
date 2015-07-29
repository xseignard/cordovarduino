## Cordovarduino

Cordovarduino is a Cordova/Phonegap plugin that enable you to use serial communication from an Android device to a serial over USB capable one.

## Change log
2014.08: [Zevero](https://github.com/zevero): Option to find device by VID and PID, that let you use "unrecognized" devices.

2014.07: [Hendrik Maus](https://github.com/hendrikmaus): Implemented writeHex for working with RS232 protocol, i.e. javascript can now pass "ff", java turns it into a 1 byte array and writes to the serial port - naturally, java, and the existing write method here, would create a 2 byte array from the input string.

2014.04: [Derek K](https://github.com/etx): Implemented registerReadCallback for evented reading and Android onPause/onResume
         
2014.03: [Ed. Lafargue](https://github.com/elafargue): Implemented read(). The success callback returns a Javascript ArrayBuffer which is the best way to handle binary data in Javascript. It is straightforward to convert this to a string if required - a utility function could be implemented in this plugin.

2013.11: [Xavier Seignard](https://github.com/xseignard): First implementation

### Install it
From the root folder of your cordova project, run :
```
cordova plugin add https://github.com/xseignard/cordovarduino.git
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
- dtr: defaults to false (it may be needed to be true for some arduino)

You're now able to read and write:
```js
serial.write(data, function success(), function error());
serial.read(function success(buffer), function error());
```
`data` is the string representation to be written to the serial port.
`buffer` is a JavaScript ArrayBuffer containing the data that was just read.

Apart from using `serial.write`, you can also use `serial.writeHex` to have an easy way to work with **RS232 protocol** driven hardware from your javascript by using **hex-strings**.

In a nutshell, `serial.writeHex('ff')` would write just a single byte where `serial.write('ff')` would let java write 2 bytes to the serial port.

Apart from that, `serial.writeHex` works the same way as `serial.write` does.

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

### Your Device is not (yet) known?

Your device might not be listed over at https://github.com/mik3y/usb-serial-for-android .
If you know your devices VID (Vendor ID) and PID (Product ID) you could however try 

```js
serial.requestPermission({vid: '1d50', pid: '607d'}, function success(), function error()); //hex strings
or
serial.requestPermission({vid: 7504, pid: 24701}, function success(), function error()); //integers
```

You can find your devices VID and PID on linux or android using "lsusb" (returning VID:PID in hex) or by looking at your dmesg log.
