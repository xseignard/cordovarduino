## cordovarduino

Want a module for your Arduino board that provides:

- Power
- High-res Touch Interface
- Storage
- *AND* connectivity? (WiFi + 3G + Bluetooth)

Hey, why not just use your Android phone/tablet? 

This Cordova/Phonegap plugin allows two-way serial communication using *USB On-The-Go* (OTG) from your Android device to your Arduino board or other USB-powered serial IO device. 

And that means that ability to give your Arduino project a mobile app (web-view) interface as well as powering it using the rechargeable battery on your phone!

### Install it
From the root folder of your cordova project, run :
```
cordova plugin add cordovarduino
```

### How to use it

Your first need to understand how to create and upload a simple Cordova Project. Here is some info on [how to get started](https://cordova.apache.org/docs/en/latest/guide/platforms/android/index.html) with Cordova on Android, and here is a [simple Cordova plugin](https://github.com/apache/cordova-plugin-vibration) you can use to get familiar with the plugin system.

The plugin API for this behaves as follows:

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
- rts: defaults to false (it may be needed to be true for some modules, including the monkeyboard dab module)
- sleepOnPause: defaults to true. If false, the the OTG port will remain open when the app goes to the background (or the screen turns off). Otherwise, the port with automatically close, and resume once the app is brought back to foreground.

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

### A Simple Example

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

### A Complete Example

Here is your `index.html`:

```html
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Security-Policy" content="default-src 'self' data: gap: https://ssl.gstatic.com 'unsafe-eval'; style-src 'self' 'unsafe-inline'; media-src *">
        <meta name="format-detection" content="telephone=no">
        <meta name="msapplication-tap-highlight" content="no">
        <meta name="viewport" content="user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1, width=device-width">
        <link rel="stylesheet" type="text/css" href="css/index.css">
        <title>Hello World</title>
    </head>
    <body>
        <div class="app">
            <h1>Potentiometer value</h1>
            <p>Value <span id="pot">...</span></p>
            <p id="delta">...</p>
            <button id="on">On</button>
            <button id="off">Off</button>
        </div>
        <script type="text/javascript" src="cordova.js"></script>
        <script type="text/javascript" src="js/index.js"></script>
    </body>
</html>
```

Here is the `index.js` file:

```js
var app = {
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    onDeviceReady: function() {
        var potText = document.getElementById('pot');
        var delta = document.getElementById('delta');
        var on = document.getElementById('on');
        var off = document.getElementById('off');
        var open = false;
        var str = '';
        var lastRead = new Date();

        var errorCallback = function(message) {
            alert('Error: ' + message);
        };
        // request permission first
        serial.requestPermission(
            // if user grants permission
            function(successMessage) {
                // open serial port
                serial.open(
                    {baudRate: 9600},
                    // if port is succesfuly opened
                    function(successMessage) {
                        open = true;
                        // register the read callback
                        serial.registerReadCallback(
                            function success(data){
                                // decode the received message
                                var view = new Uint8Array(data);
                                if(view.length >= 1) {
                                    for(var i=0; i < view.length; i++) {
                                        // if we received a \n, the message is complete, display it
                                        if(view[i] == 13) {
                                            // check if the read rate correspond to the arduino serial print rate
                                            var now = new Date();
                                            delta.innerText = now - lastRead;
                                            lastRead = now;
                                            // display the message
                                            var value = parseInt(str);
                                            pot.innerText = value;
                                            str = '';
                                        }
                                        // if not, concatenate with the begening of the message
                                        else {
                                            var temp_str = String.fromCharCode(view[i]);
                                            var str_esc = escape(temp_str);
                                            str += unescape(str_esc);
                                        }
                                    }
                                }
                            },
                            // error attaching the callback
                            errorCallback
                        );
                    },
                    // error opening the port
                    errorCallback
                );
            },
            // user does not grant permission
            errorCallback
        );

        on.onclick = function() {
            console.log('click');
            if (open) serial.write('1');
        };
        off.onclick = function() {
            if (open) serial.write('0');
        }
    }
};

app.initialize();
```

And here is your Arduino project `.ino` file, with a potentiometer on A0 and a led on 13:

```c
#define POT A0
#define LED 13

unsigned long previousMillis;
int interval = 50;

void setup() {
    Serial.begin(9600);
    pinMode(POT, INPUT);
    pinMode(LED, OUTPUT);
}

void loop() {
    if (Serial.available() > 0) {
        char i = Serial.read();
        switch (i) {
            case '0':
                digitalWrite(LED, LOW);
                break;
            case '1':
                digitalWrite(LED, HIGH);
                break;
        }
    }
    if (millis() - previousMillis >= interval) {
        previousMillis = millis();
        int value = analogRead(POT);
        Serial.println(value);
    }
}
```

### Your Device is not (yet) known?

Thanks to [usb-serial-for-android](https://github.com/mik3y/usb-serial-for-android) library, you can communicate with CDC, FTDI, Arduino and other devices. 

Your device might not be listed over at https://github.com/mik3y/usb-serial-for-android .
If you know your devices VID (Vendor ID) and PID (Product ID) you could however try 

```js
serial.requestPermission({vid: '1d50', pid: '607d'}, function success(), function error()); //hex strings
or
serial.requestPermission({vid: 7504, pid: 24701}, function success(), function error()); //integers
```

You can also choose the driver to use. Options are:
- `CdcAcmSerialDriver`
- `Ch34xSerialDriver`
- `Cp21xxSerialDriver`
- `FtdiSerialDriver`
- `ProlificSerialDriver`


It defaults to `CdcAcmSerialDriver` if empty or not one of these (please feel free to add a PR to support more).

```js
serial.requestPermission({
        vid: '1d50',
        pid: '607d',
        driver: 'FtdiSerialDriver' // or any other
    },
    function success(),
    function error()
);
```

You can find your devices VID and PID on linux or android using "lsusb" (returning VID:PID in hex) or by looking at your dmesg log.


## Change log
2015.10: [Ed. Lafargue](https://github.com/elafargue): Implemented "sleepOnPause" flag in the 'open' options to prevent closing the OTG port when app goes to background.

2014.08: [Zevero](https://github.com/zevero): Option to find device by VID and PID, that let you use "unrecognized" devices.

2014.07: [Hendrik Maus](https://github.com/hendrikmaus): Implemented writeHex for working with RS232 protocol, i.e. javascript can now pass "ff", java turns it into a 1 byte array and writes to the serial port - naturally, java, and the existing write method here, would create a 2 byte array from the input string.

2014.04: [Derek K](https://github.com/etx): Implemented registerReadCallback for evented reading and Android onPause/onResume
         
2014.03: [Ed. Lafargue](https://github.com/elafargue): Implemented read(). The success callback returns a Javascript ArrayBuffer which is the best way to handle binary data in Javascript. It is straightforward to convert this to a string if required - a utility function could be implemented in this plugin.

2013.11: [Xavier Seignard](https://github.com/xseignard): First implementation
