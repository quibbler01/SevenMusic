package com.google.zxing.camera

import android.hardware.Camera
import android.os.Handler
import android.util.Log

class PreviewCallback internal constructor(
    private val configManager: CameraConfigurationManager,
    private val useOneShotPreviewCallback: Boolean
) : Camera.PreviewCallback {
    private var previewHandler: Handler? = null
    private var previewMessage = 0

    fun setHandler(previewHandler: Handler?, previewMessage: Int) {
        this.previewHandler = previewHandler
        this.previewMessage = previewMessage
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera) {
        val cameraResolution = configManager.getCameraResolution()
        if (!useOneShotPreviewCallback) {
            camera.setPreviewCallback(null)
        }
        if (previewHandler != null) {
            val message = previewHandler!!.obtainMessage(
                previewMessage, cameraResolution.x,
                cameraResolution.y, data
            )
            message.sendToTarget()
            previewHandler = null
        } else {
            Log.d(TAG, "Got preview callback, but no handler for it")
        }
    }

    companion object {
        private val TAG: String = PreviewCallback::class.java.getSimpleName()
    }
}
