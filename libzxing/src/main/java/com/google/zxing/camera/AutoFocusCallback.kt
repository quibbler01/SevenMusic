package com.google.zxing.camera

import android.hardware.Camera
import android.os.Handler
import android.util.Log

class AutoFocusCallback : Camera.AutoFocusCallback {
    private var autoFocusHandler: Handler? = null
    private var autoFocusMessage = 0

    fun setHandler(autoFocusHandler: Handler?, autoFocusMessage: Int) {
        this.autoFocusHandler = autoFocusHandler
        this.autoFocusMessage = autoFocusMessage
    }

    override fun onAutoFocus(success: Boolean, camera: Camera?) {
        if (autoFocusHandler != null) {
            val message = autoFocusHandler!!.obtainMessage(autoFocusMessage, success)
            autoFocusHandler!!.sendMessageDelayed(message, AUTOFOCUS_INTERVAL_MS)
            autoFocusHandler = null
        } else {
            Log.d(TAG, "Got auto-focus callback, but no handler for it")
        }
    }

    companion object {
        private val TAG: String = AutoFocusCallback::class.java.getSimpleName()

        private const val AUTOFOCUS_INTERVAL_MS = 1500L
    }
}
