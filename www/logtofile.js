var exec = cordova.require('cordova/exec');

module.exports = {
    writeLine: function (line, callback) {
        callback = callback || function () { };
        exec(callback.bind(null), // success
             callback.bind(null), // failure
             'LogToFile',
             'writeLine',
             [line]
        );
    }
};
