package org.apache.cordova.logtofile;

import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Environment;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.LoggerFactory;

import java.io.File;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.util.StatusPrinter;

public class LogToFile extends CordovaPlugin {
    private static String LOGFILE_PATH;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(LogToFile.class);

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    private void configureLogger() {
        // reset the default context (which may already have been initialized)
        // since we want to reconfigure it
        LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
        lc.reset();

        // setup FileAppender
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(lc);
        encoder.setPattern("%d{yyyy-dd-MM HH:mm:ss.SSS} : %-5level : %msg%n");
        encoder.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
        fileAppender.setContext(lc);
        //fileAppender.setLazy(true);
        fileAppender.setAppend(true);
        fileAppender.setName("FILE");
        fileAppender.setFile(LOGFILE_PATH);
        fileAppender.setEncoder(encoder);
        fileAppender.start();

        LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(lc);
        logcatAppender.setName("LOGCAT");
        logcatAppender.setEncoder(encoder);
        logcatAppender.start();

        // add the newly created appenders to the root logger;
        // qualify Logger to disambiguate from org.slf4j.Logger
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.ALL);
        root.addAppender(fileAppender);
        root.addAppender(logcatAppender);

        StatusPrinter.print(lc);
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
                        callbackContext.error("Logger exception:" + e.toString());
                    }
                }
            });

        } else if (action.equals("info")) {
            final String line = data.getString(0);
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        log.info(line);
                        callbackContext.success();
                    } catch (Exception e) {
                        callbackContext.error("Logger exception:" + e.toString());
                    }
                }
            });

        } else if (action.equals("warn")) {
            final String line = data.getString(0);
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        log.warn(line);
                        callbackContext.success();
                    } catch (Exception e) {
                        callbackContext.error("Logger exception:" + e.toString());
                    }
                }
            });

        } else if (action.equals("error")) {
            final String line = data.getString(0);
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        log.error(line);
                        callbackContext.success();
                    } catch (Exception e) {
                        callbackContext.error("Logger exception:" + e.toString());
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
                            callbackContext.success(Uri.fromFile(new File(LOGFILE_PATH)).toString());
                        } catch (Exception e) {
                            callbackContext.error("Logger exception:" + e.toString());
                        }
                    }
                });

        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // assume SLF4J is bound to logback-classic in the current environment
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.stop();
    }

}
