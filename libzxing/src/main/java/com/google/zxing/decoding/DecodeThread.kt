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

import android.os.Handler
import android.os.Looper
import com.google.zxing.BarcodeFormat
import com.google.zxing.CaptureFragment
import com.google.zxing.DecodeHintType
import com.google.zxing.ResultPointCallback
import java.util.Hashtable
import java.util.Vector
import java.util.concurrent.CountDownLatch

/**
 * This thread does all the heavy lifting of decoding the images.
 */
internal class DecodeThread(
    fragment: CaptureFragment,
    decodeFormats: Vector<BarcodeFormat?>?,
    characterSet: String?,
    resultPointCallback: ResultPointCallback?
) : Thread() {
    private val fragment: CaptureFragment
    private val hints: Hashtable<DecodeHintType?, Any?>
    private var handler: Handler? = null
    private val handlerInitLatch: CountDownLatch

    init {
        var decodeFormats = decodeFormats
        this.fragment = fragment
        handlerInitLatch = CountDownLatch(1)

        hints = Hashtable<DecodeHintType?, Any?>(3)

        if (decodeFormats == null || decodeFormats.isEmpty()) {
            decodeFormats = Vector<BarcodeFormat?>()
            decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS)
            decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS)
            decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS)
        }

        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats)

        if (characterSet != null) {
            hints.put(DecodeHintType.CHARACTER_SET, characterSet)
        }

        hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback)
    }

    fun getHandler(): Handler? {
        try {
            handlerInitLatch.await()
        } catch (ie: InterruptedException) {
            // continue?
        }
        return handler
    }

    override fun run() {
        Looper.prepare()
        handler = DecodeHandler(fragment, hints)
        handlerInitLatch.countDown()
        Looper.loop()
    }

    companion object {
        const val BARCODE_BITMAP: String = "barcode_bitmap"
    }
}
