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
    write: function (line, successCb, failureCb) {
        exec(successCb, // success
             failureCb, // failure
             'LogToFile',
             'write',
             [line]
        );
    }
};
