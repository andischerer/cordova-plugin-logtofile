# cordova-plugin-logtofile

> Cordova plugin for writing logs to the filesystem.

Logger uses [logback-android](https://github.com/tony19/logback-android) internally.

## Installation

```bash
cordova plugin add cordova-plugin-logtofile
```

## Usage

```javascript
// setup a logfile path (required)
// this path is relative to your device sdcard storage directory
window.logToFile.setLogfilePath('/myapp/log.txt', function () {
    // logger configured successfully  
}, function (err) {
    // logfile could not be written
    // handle error
});

// get the logfilePath from the currently running logger instance
window.logToFile.getLogfilePath(function (logfilePath) {
    // dosomething with the logfilepath
}, function (err) {
    // handle error
});

// get the all archived logfile paths as array
window.logToFile.getArchivedLogfilePaths(function (archivedlogfiles) {
    // dosomething with the archived logs
}, function (err) {
    // handle error
});

// write logmessages in different loglevels
window.logToFile.debug('Sample debug message');
window.logToFile.info('Sample info message');
window.logToFile.warn('Sample warn message');
window.logToFile.error('Sample error message');
```

## Logrotate
If the logfile exceeds a filesize above 5MB the current logfile will be archived and zipped into the current logfiledirectory. The last 3 archived logfiles will be retained, older logs get deleted.
The archived logfiles are reachable under the following filepattern: `{logfiledirectory}/{logfilename}.{index}.zip`

## Currently supported Platforms
- Android

## TODOs
- add more config options