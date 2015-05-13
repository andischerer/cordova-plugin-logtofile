package org.apache.cordova.logtofile;

import android.os.Environment;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import de.mindpipe.android.logging.log4j.LogConfigurator;

public class LogToFile extends CordovaPlugin {
    private static final String TAG = LogToFile.class.getSimpleName();
    public final static String LOGFILE_PATH = "/test.txt";

    Logger log;
    LogConfigurator logConfigurator;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        // Logger Config
        if (logConfigurator == null) {
            logConfigurator = new LogConfigurator();
        }

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            logConfigurator.setFileName(Environment.getExternalStorageDirectory() + LOGFILE_PATH);
            logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
            logConfigurator.setMaxFileSize(1024 * 1024 * 5);
            logConfigurator.setMaxBackupSize(3);
            logConfigurator.setUseFileAppender(true);
            logConfigurator.setImmediateFlush(true);
        } else {
            logConfigurator.setUseFileAppender(false);
        }
        logConfigurator.setRootLevel(Level.DEBUG);
        logConfigurator.setLevel("org.apache", Level.ERROR);
        logConfigurator.setUseLogCatAppender(true);
        logConfigurator.configure();

        log = Logger.getLogger(LogToFile.class);
    }

    @Override
    public boolean execute(String action, JSONArray data, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("writeLine")) {
            final String line = data.getString(0);

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        log.debug(line);
                        callbackContext.success();
                    } catch (Exception e) {
                        Log.d(TAG, "Log exception:" + e.toString());
                        callbackContext.error("Log exception:" + e.toString());
                    }
                }
            });

        } else {
            return false;
        }
        return true;
    }
}
