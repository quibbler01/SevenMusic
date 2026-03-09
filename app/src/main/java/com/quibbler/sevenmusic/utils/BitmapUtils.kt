package com.quibbler.sevenmusic.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Package:        com.quibbler.sevenmusic.presenter
 * ClassName:      BitmapUtils
 * Description:    处理bitmap的类
 * Author:         yanwuyang
 * CreateDate:     2019/9/30 15:37
 */
object BitmapUtils {
    private const val TAG = "BitmapUtils"

    /**
     * 将bitmap转为圆角
     * 
     * @param bitmap 待处理的bitmap
     * @return 处理后的bitmap
     */
    fun makeRoundCorner(bitmap: Bitmap): Bitmap {
        val width = bitmap.getWidth()
        val height = bitmap.getHeight()
        var left = 0
        var top = 0
        var right = width
        var bottom = height
        var roundPx = (height / 2).toFloat()
        if (width > height) {
            left = (width - height) / 2
            top = 0
            right = left + height
            bottom = height
        } else if (height > width) {
            left = 0
            top = (height - width) / 2
            right = width
            bottom = top + width
            roundPx = (width / 2).toFloat()
        }
        Log.i(TAG, "ps:" + left + ", " + top + ", " + right + ", " + bottom)

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(left, top, right, bottom)
        val rectF = RectF(rect)

        paint.setAntiAlias(true)
        canvas.drawARGB(0, 0, 0, 0)
        paint.setColor(color)
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    /**
     * 将数据解析成低画质的bitmap以节省内存空间
     * 
     * @param data
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    fun decodeLowQualityBitmap(data: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(data, 0, data.size, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.RGB_565
        return BitmapFactory.decodeByteArray(data, 0, data.size, options)
    }

    fun decodeLowQualityBitmap(inputStream: InputStream, reqWidth: Int, reqHeight: Int): Bitmap? {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        var n = 0
        while (true) {
            try {
                if (-1 == (inputStream.read(buffer).also { n = it })) break
            } catch (e: IOException) {
                e.printStackTrace()
            }
            output.write(buffer, 0, n)
        }
        return decodeLowQualityBitmap(output.toByteArray(), reqWidth, reqHeight)
    }


    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
