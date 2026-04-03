package com.quibbler.sevenmusic.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

/**
 * 
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      ColorFilterTransformation
 * Description:    加载图片颜色变换，配合glide使用
 * Author:         lishijun
 * CreateDate:     2019/10/10 10:36
 */
class ColorFilterTransformation(color: Int) : BitmapTransformation() {
    private val mColor: Int

    init {
        mColor = color
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val width = toTransform.getWidth()
        val height = toTransform.getHeight()

        val config =
            toTransform.getConfig() ?: Bitmap.Config.ARGB_8888
        var bitmap = pool.get(width, height, config)
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, config)
        }
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.setAntiAlias(true)
        paint.setColorFilter(PorterDuffColorFilter(mColor, PorterDuff.Mode.SRC_ATOP))
        canvas.drawBitmap(toTransform, 0f, 0f, paint)

        return bitmap
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update((ID + mColor).toByteArray(CHARSET))
    }

    companion object {
        private const val VERSION = 1
        private val ID = "ColorFilterTransformation." + VERSION
    }
}
