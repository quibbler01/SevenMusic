/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.zxing.decoding

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.os.Parcelable
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.CaptureFragment
import com.google.zxing.R
import com.google.zxing.Result
import com.google.zxing.camera.CameraManager
import com.google.zxing.view.ViewfinderResultPointCallback
import com.google.zxing.view.ViewfinderView
import java.util.Vector

/**
 * This class handles all the messaging which comprises the state machine for capture.
 */
class CaptureActivityHandler(
    private val fragment: CaptureFragment, decodeFormats: Vector<BarcodeFormat?>?,
    characterSet: String?, viewfinderView: ViewfinderView?
) : Handler() {
    private val decodeThread: DecodeThread
    private var state: State?

    private enum class State {
        PREVIEW,
        SUCCESS,
        DONE
    }

    init {
        decodeThread = DecodeThread(
            fragment, decodeFormats, characterSet,
            ViewfinderResultPointCallback(viewfinderView)
        )
        decodeThread.start()
        state = State.SUCCESS
        // Start ourselves capturing previews and decoding.
        CameraManager.get()!!.startPreview()
        restartPreviewAndDecode()
    }

    override fun handleMessage(message: Message) {
        if (message.what == R.id.auto_focus) {
            //Log.d(TAG, "Got auto-focus message");
            // When one auto focus pass finishes, start another. This is the closest thing to
            // continuous AF. It does seem to hunt a bit, but I'm not sure what else to do.
            if (state == State.PREVIEW) {
                CameraManager.get()!!.requestAutoFocus(this, R.id.auto_focus)
            }
        } else if (message.what == R.id.restart_preview) {
            Log.d(TAG, "Got restart preview message")
            restartPreviewAndDecode()
        } else if (message.what == R.id.decode_succeeded) {
            Log.d(TAG, "Got decode succeeded message")
            state = State.SUCCESS
            val bundle = message.getData()

            /** */
            val barcode =
                if (bundle == null) null else bundle.getParcelable<Parcelable?>(DecodeThread.Companion.BARCODE_BITMAP) as Bitmap? //

            fragment.handleDecode(message.obj as Result?, barcode)
            /** */
        } else if (message.what == R.id.decode_failed) {
            // We're decoding as fast as possible, so when one decode fails, start another.
            state = State.PREVIEW
            CameraManager.get()!!
                .requestPreviewFrame(decodeThread.getHandler(), R.id.decode)
        } else if (message.what == R.id.return_scan_result) {
            Log.d(TAG, "Got return scan result message")
            fragment.getActivity()!!.setResult(Activity.RESULT_OK, message.obj as Intent?)
            fragment.getActivity()!!.finish()
        } else if (message.what == R.id.launch_product_query) {
            Log.d(TAG, "Got product query message")
            val url = message.obj as String?
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
            fragment.getActivity()!!.startActivity(intent)
        }
    }

    fun quitSynchronously() {
        state = State.DONE
        CameraManager.get()!!.stopPreview()
        val quit = Message.obtain(decodeThread.getHandler(), R.id.quit)
        quit.sendToTarget()
        try {
            decodeThread.join()
        } catch (e: InterruptedException) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded)
        removeMessages(R.id.decode_failed)
    }

    private fun restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW
            CameraManager.get()!!
                .requestPreviewFrame(decodeThread.getHandler(), R.id.decode)
            CameraManager.get()!!.requestAutoFocus(this, R.id.auto_focus)
            fragment.drawViewfinder()
        }
    }

    companion object {
        private val TAG: String = CaptureActivityHandler::class.java.getSimpleName()
    }
}
