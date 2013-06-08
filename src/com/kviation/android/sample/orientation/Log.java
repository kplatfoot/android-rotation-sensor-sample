package com.kviation.android.sample.orientation;

public class Log {
  private static final String LOGTAG = "AttitudeIndicator";
  private static final boolean LOGV = false;

  public static void v(String msg, Object... args) {
    if (LOGV) {
      android.util.Log.v(LOGTAG, String.format(msg, args));
    }
  }

  public static void i(String msg, Object... args) {
    android.util.Log.i(LOGTAG, String.format(msg, args));
  }

  public static void w(String msg, Object... args) {
    android.util.Log.w(LOGTAG, String.format(msg, args));
  }

  public static void w(String msg, Throwable ex) {
    android.util.Log.w(LOGTAG, msg, ex);
  }

  public static void e(String msg) {
    android.util.Log.e(LOGTAG, msg);
  }

  public static void e(String msg, Throwable ex) {
    android.util.Log.e(LOGTAG, msg, ex);
  }
}
