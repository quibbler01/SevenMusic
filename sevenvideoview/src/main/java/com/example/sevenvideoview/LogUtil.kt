package com.example.sevenvideoview

import android.util.Log

/**
 * log工具.
 */
object LogUtil {
    private const val TAG = "SevenVideoPlayer"

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun i(message: String) {
        Log.i(TAG, message)
    }

    fun e(message: String?, throwable: Throwable?) {
        Log.e(TAG, message, throwable)
    }
}
