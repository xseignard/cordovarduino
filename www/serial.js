var serial = {
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
    read: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'Serial',
            'readSerial',
            []
        );
    },
    suscribe: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'Serial',
            'suscribe',
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
    }
};
module.exports = serial;
