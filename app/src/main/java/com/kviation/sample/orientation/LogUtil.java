package com.kviation.sample.orientation;

import android.util.Log;

/**
 * Utility functions for logging.
 */
public class LogUtil {

  public static final boolean LOGV = false; // Only set for debugging

  private static final String TAG = "AttitudeIndicator";

  public static void v(String msg, Object... args) {
    if (!LOGV) {
      return;
    }
    if (args.length > 0) {
      msg = String.format(msg, args);
    }
    Log.v(TAG, msg);
  }

  public static void i(String msg, Object... args) {
    if (args.length > 0) {
      msg = String.format(msg, args);
    }
    Log.i(TAG, msg);
  }

  public static void w(String msg, Object... args) {
    if (args.length > 0) {
      msg = String.format(msg, args);
    }
    Log.w(TAG, msg);
  }

  public static void e(String msg, Object... args) {
    if (args.length > 0) {
      msg = String.format(msg, args);
    }
    Log.e(TAG, msg);
  }

  public static void e(Throwable t, String msg, Object... args) {
    if (args.length > 0) {
      msg = String.format(msg, args);
    }
    Log.e(TAG, msg, t);
  }
}
