package org.apache.cordova.logtofile;

import android.content.pm.ApplicationInfo;
import android.os.Environment;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class LogToFile extends CordovaPlugin {
    private static final String TAG = LogToFile.class.getSimpleName();
    private static String LOGFILE_PATH;

    Logger log;
    LogConfigurator logConfigurator;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        ApplicationInfo info = cordova.getActivity().getApplicationInfo();
        LOGFILE_PATH = info.dataDir + "/log.txt";

        // Logger Config
        if (logConfigurator == null) {
            logConfigurator = new LogConfigurator();
        }

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            logConfigurator.setFileName(LOGFILE_PATH);
            //logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
            logConfigurator.setFilePattern("%d %-5p %m%n");
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

    private static String pathCombine(String path1, String path2) {
        File parent = new File(path1);
        File child = new File(parent, path2);
        return child.getPath();
    }

    @Override
    public boolean execute(String action, JSONArray data, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("write")) {
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

        } else if (action.equals("setLogfilePath")) {
            final String logfilePath = data.getString(0);
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        LOGFILE_PATH = pathCombine(Environment.getExternalStorageDirectory().getAbsolutePath(), logfilePath);
                        logConfigurator.setFileName(LOGFILE_PATH);
                        logConfigurator.setResetConfiguration(true);
                        logConfigurator.configure();
                        callbackContext.success(LOGFILE_PATH);
                    } catch (Exception e) {
                        Log.d(TAG, "Log exception:" + e.toString());
                        callbackContext.error("Log exception:" + e.toString());
                    }
                }
            });

        } else if (action.equals("getLogfilePath")) {
                cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        try {
                            callbackContext.success(LOGFILE_PATH);
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
