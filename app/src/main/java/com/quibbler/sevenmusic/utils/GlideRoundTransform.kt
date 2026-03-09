package com.quibbler.sevenmusic.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import java.security.MessageDigest

class GlideRoundTransform @JvmOverloads constructor(context: Context?, dp: Int = 10) :
    CenterCrop() {
    init {
        radius = dp.toFloat()
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap? {
        //glide4.0+
        val transform = super.transform(pool, toTransform, outWidth, outHeight)
        return roundCrop(pool, transform)
        //glide3.0
        //return roundCrop(pool, toTransform);
    }

    val id: String
        get() = javaClass.getName() + Math.round(radius)

    override fun updateDiskCacheKey(messageDigest: MessageDigest?) {
    }

    companion object {
        private var radius = 10f

        private fun roundCrop(pool: BitmapPool, source: Bitmap?): Bitmap? {
            if (source == null) return null

            var result = pool.get(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888)
            if (result == null) {
                result = Bitmap.createBitmap(
                    source.getWidth(),
                    source.getHeight(),
                    Bitmap.Config.ARGB_8888
                )
            }

            val canvas = Canvas(result)
            val paint = Paint()
            paint.setShader(
                BitmapShader(
                    source,
                    BitmapShader.TileMode.CLAMP,
                    BitmapShader.TileMode.CLAMP
                )
            )
            paint.setAntiAlias(true)
            val rectF = RectF(0f, 0f, source.getWidth().toFloat(), source.getHeight().toFloat())
            canvas.drawRoundRect(rectF, radius, radius, paint)
            return result
        }
    }
}
