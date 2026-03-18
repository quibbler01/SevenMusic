package com.google.zxing.camera

import android.graphics.Bitmap
import com.google.zxing.LuminanceSource

/**
 * Created by aaron on 16/7/27.
 * 自定义解析Bitmap LuminanceSource
 */
class BitmapLuminanceSource(bitmap: Bitmap) :
    LuminanceSource(bitmap.getWidth(), bitmap.getHeight()) {
    private val bitmapPixels: ByteArray?

    init {
        // 首先，要取得该图片的像素数组内容
        val data = IntArray(bitmap.getWidth() * bitmap.getHeight())
        this.bitmapPixels = ByteArray(bitmap.getWidth() * bitmap.getHeight())
        bitmap.getPixels(data, 0, getWidth(), 0, 0, getWidth(), getHeight())

        // 将int数组转换为byte数组，也就是取像素值中蓝色值部分作为辨析内容
        for (i in data.indices) {
            this.bitmapPixels[i] = data[i].toByte()
        }
    }

    override fun getMatrix(): ByteArray? {
        // 返回我们生成好的像素数据
        return bitmapPixels
    }

    override fun getRow(y: Int, row: ByteArray): ByteArray {
        // 这里要得到指定行的像素数据
        System.arraycopy(bitmapPixels, y * getWidth(), row, 0, getWidth())
        return row
    }
}
