var exec = cordova.require('cordova/exec');

module.exports = {
    setLogfilePath: function (path, successCb, failureCb) {
        exec(successCb, // success
             failureCb, // failure
             'LogToFile',
             'setLogfilePath',
             [path]
        );
    },
    getLogfilePath: function (successCb, failureCb) {
        exec(successCb, // success
             failureCb, // failure
             'LogToFile',
             'getLogfilePath',
             []
        );
    },
    debug: function (line, successCb, failureCb) {
        exec(successCb, // success
             failureCb, // failure
             'LogToFile',
             'debug',
             [line]
        );
    },
    info: function (line, successCb, failureCb) {
        exec(successCb, // success
             failureCb, // failure
             'LogToFile',
             'info',
             [line]
        );
    },
    warn: function (line, successCb, failureCb) {
        exec(successCb, // success
             failureCb, // failure
             'LogToFile',
             'warn',
             [line]
        );
    },
    error: function (line, successCb, failureCb) {
        exec(successCb, // success
             failureCb, // failure
             'LogToFile',
             'error',
             [line]
        );
    }
};
