var serial = {
    requestPermission: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'Serial',
            'requestPermission',
            []
        );
    },
    open: function(opts, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'Serial',
            'openSerial',
            [{'opts': opts}]
        );
    },
    write: function(data, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'Serial',
            'writeSerial',
            [{'data': data}]
        );
    },
    writeHex: function(hexString, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'Serial',
            'writeSerialHex',
            [{'data': data}]
        );
    },
    read: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'Serial',
            'readSerial',
            []
        );
    },
    close: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'Serial',
            'closeSerial',
            []
        );
    },
    registerReadCallback: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'Serial',
            'registerReadCallback',
            []
        );
    }

};
module.exports = serial;
