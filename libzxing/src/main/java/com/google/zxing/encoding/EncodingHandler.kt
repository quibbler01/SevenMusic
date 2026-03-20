package com.google.zxing.encoding

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.util.Hashtable

object EncodingHandler {
    private const val BLACK = -0x1000000

    @Throws(WriterException::class)
    fun createQRCode(str: String?, widthAndHeight: Int): Bitmap {
        val hints = Hashtable<EncodeHintType?, String?>()
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8")
        val matrix = MultiFormatWriter().encode(
            str,
            BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight
        )
        val width = matrix.getWidth()
        val height = matrix.getHeight()
        val pixels = IntArray(width * height)

        for (y in 0..<height) {
            for (x in 0..<width) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = BLACK
                }
            }
        }
        val bitmap = Bitmap.createBitmap(
            width, height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
}
