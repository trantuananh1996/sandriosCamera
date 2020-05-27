package com.sandrios.sandriosCamera.internal.utils;

import android.util.Log;

import com.sandrios.sandriosCamera.BuildConfig;

import java.util.function.Function;


public class LogUtils {
    public static void e(String TAG, String content) {
        if (BuildConfig.DEBUG) Log.e(TAG, content);
        //else Crashlytics.log(Log.DEBUG, TAG, content);
    }

    public static void e(String TAG, Function<Void, String> function) {
        if (BuildConfig.DEBUG) {
            String content = function.apply(null);
            Log.e(TAG, content);
        } //else Crashlytics.log(Log.DEBUG, TAG, function.apply(null));
    }

    public static void e(Throwable e) {
        if (BuildConfig.DEBUG && e != null) e.printStackTrace();
        //   else if (e != null) Crashlytics.logException(e);
    }

    static String className;
    static String methodName;
    static int lineNumber;

    public LogUtils() {
        /* Protect from instantiations */
    }

    public static boolean isDebuggable() {
        return BuildConfig.DEBUG;
    }

    private static String createLog(String log) {

        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        buffer.append(methodName);
        buffer.append(":");
        buffer.append(lineNumber);
        buffer.append("]");
        buffer.append(log);

        return buffer.toString();
    }

    private static void getMethodNames(StackTraceElement[] sElements) {
        className = sElements[1].getFileName();
        methodName = sElements[1].getMethodName();
        lineNumber = sElements[1].getLineNumber();
    }

    public static void e(String message) {
        if (!isDebuggable())
            return;

        // Throwable instance must be created before any methods
        getMethodNames(new Throwable().getStackTrace());
        Log.e(className, createLog(message));
    }

    public static void i(String message) {
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.i(className, createLog(message));
    }

    public static void d(String message) {
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.d(className, createLog(message));
    }

    public static void v(String message) {
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.v(className, createLog(message));
    }

    public static void w(String message) {
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.w(className, createLog(message));
    }

    public static void wtf(String message) {
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.wtf(className, createLog(message));
    }
}
