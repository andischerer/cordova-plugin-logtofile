package org.apache.cordova.logtofile;

import android.Manifest;
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
import java.io.FileNotFoundException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;

public class LogToFile extends CordovaPlugin {
    private static String LOGFILE_PATH;

    public static final String PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final int WRITE_STORAGE_REQ_CODE = 0;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(LogToFile.class);

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    private void configureLogger() {
        File targetFile = new File(LOGFILE_PATH);

        // reset the default context (which may already have been initialized)
        // since we want to reconfigure it
        LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
        lc.reset();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(lc);
        encoder.setPattern("%d{yyyy-dd-MM HH:mm:ss.SSS} : %-5level : %msg%n");
        encoder.start();

        String filePattern = targetFile.getParent() + "/" + targetFile.getName() + ".%i.zip";
        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setFileNamePattern(filePattern);
        rollingPolicy.setMinIndex(1);
        rollingPolicy.setMaxIndex(3);
        rollingPolicy.setContext(lc);

        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
        triggeringPolicy.setMaxFileSize("5MB");
        triggeringPolicy.setContext(lc);

        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
        rollingFileAppender.setAppend(true);
        rollingFileAppender.setName("FILE");
        rollingFileAppender.setContext(lc);
        rollingFileAppender.setFile(LOGFILE_PATH);
        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.setTriggeringPolicy(triggeringPolicy);
        rollingFileAppender.setEncoder(encoder);

        triggeringPolicy.start();
        rollingPolicy.setParent(rollingFileAppender);
        rollingPolicy.start();
        rollingFileAppender.start();

        // Logcat appender
        LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(lc);
        logcatAppender.setName("LOGCAT");
        logcatAppender.setEncoder(encoder);
        logcatAppender.start();

        // add the newly created appenders to the root logger;
        // qualify Logger to disambiguate from org.slf4j.Logger
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.ALL);
        root.addAppender(rollingFileAppender);
        root.addAppender(logcatAppender);
    }

    private static String pathCombine(String path1, String path2) {
        File parent = new File(path1);
        File child = new File(parent, path2);
        return child.getPath();
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private boolean checkPermission() {
        if(!cordova.hasPermission(PERMISSION_WRITE_STORAGE)) {
            cordova.requestPermission(this, WRITE_STORAGE_REQ_CODE, PERMISSION_WRITE_STORAGE);
            throw new SecurityException(PERMISSION_WRITE_STORAGE + " has not been granted.");
        }
        return true;
    }

    @Override
    public boolean execute(String action, JSONArray data, final CallbackContext callbackContext) throws JSONException {

        if (action.equals("debug")) {
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
                        if (isExternalStorageWritable() && checkPermission()) {
                            LOGFILE_PATH = pathCombine(Environment.getExternalStorageDirectory().getAbsolutePath(), logfilePath);
                            configureLogger();
                            callbackContext.success(LOGFILE_PATH);
                        } else {
                            callbackContext.error("Logger Error: Could not write logfile.");
                        }
                    } catch (Exception e) {
                        callbackContext.error("Logger exception:" + e.toString());
                    }
                }
            });

        } else if (action.equals("getLogfilePath")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        if (LOGFILE_PATH != null) {
                            File logfile = new File(LOGFILE_PATH);
                            if (checkPermission() && logfile.exists()) {
                                callbackContext.success(Uri.fromFile(logfile).toString());
                            } else {
                                throw new FileNotFoundException(Uri.fromFile(logfile).toString() + " could not be found.");
                            }
                        } else {
                            throw new FileNotFoundException("LogfilePath is not set");
                        }
                    } catch (Exception e) {
                        callbackContext.error("Logger exception:" + e.toString());
                    }
                }
            });

        } else if (action.equals("getArchivedLogfilePaths")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        if (LOGFILE_PATH != null) {
                            File logfile = new File(LOGFILE_PATH);
                            if (checkPermission() && logfile.exists()) {
                                String logfileDir = logfile.getParent();
                                JSONArray archivedfiles = new JSONArray();
                                File files[] = new File(logfileDir).listFiles();
                                if (files != null) {
                                    for (File file : files) {
                                        if (!file.isDirectory() && file.exists()) {
                                            if (file.getName().endsWith(".zip")) {
                                                archivedfiles.put(Uri.fromFile(file));
                                            }
                                        }
                                    }
                                }
                                callbackContext.success(archivedfiles);
                            } else {
                                throw new FileNotFoundException(Uri.fromFile(logfile).toString() + " could not be found.");
                            }
                        } else {
                            throw new FileNotFoundException("LogfilePath is not set");
                        }
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
