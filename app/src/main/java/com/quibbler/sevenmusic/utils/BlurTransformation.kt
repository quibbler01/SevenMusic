package com.quibbler.sevenmusic.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      BlurTransformation
 * Description:    高斯模糊（结合glide使用）
 * Author:         lishijun
 * CreateDate:     2019/10/7 15:39
 */
class BlurTransformation @JvmOverloads constructor(
    radius: Int = MAX_RADIUS,
    sampling: Int = DEFAULT_DOWN_SAMPLING
) : BitmapTransformation() {
    private val mRadius: Int
    private val mSampling: Int

    init {
        this.mRadius = radius
        this.mSampling = sampling
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val width = toTransform.getWidth()
        val height = toTransform.getHeight()
        val scaledWidth = width / mSampling
        val scaledHeight = height / mSampling

        var bitmap: Bitmap? = pool.get(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap!!)
        canvas.scale(1 / mSampling.toFloat(), 1 / mSampling.toFloat())
        val paint = Paint()
        paint.setFlags(Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(toTransform, 0f, 0f, paint)
        bitmap = FastBlur.blur(bitmap, mRadius, true)

        //加一点黑色背景，防止纯白背景导致字体不清晰
        val canvas2 = Canvas(bitmap!!)
        val paint2 = Paint()
        paint2.setAntiAlias(true)
        paint2.setColorFilter(PorterDuffColorFilter(0x66000000, PorterDuff.Mode.SRC_ATOP))
        canvas2.drawBitmap(bitmap, 0f, 0f, paint2)

        return bitmap
    }

    override fun toString(): String {
        return "BlurTransformation(mRadius=" + mRadius + ", mSampling=" + mSampling + ")"
    }

    override fun equals(o: Any?): Boolean {
        return o is BlurTransformation && o.mRadius == mRadius && o.mSampling == mSampling
    }

    override fun hashCode(): Int {
        return ID.hashCode() + mRadius * 1000 + mSampling * 10
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update((ID + mRadius + mSampling).toByteArray(CHARSET))
    }

    companion object {
        private const val VERSION = 1
        private val ID = "BlurTransformation." + VERSION

        private const val MAX_RADIUS = 25
        private const val DEFAULT_DOWN_SAMPLING = 1
    }
}


internal object FastBlur {
    fun blur(sentBitmap: Bitmap, radius: Int, canReuseInBitmap: Boolean): Bitmap? {
        val bitmap: Bitmap
        if (canReuseInBitmap) {
            bitmap = sentBitmap
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig() ?: Bitmap.Config.ARGB_8888, true)
        }

        if (radius < 1) {
            return (null)
        }

        val w = bitmap.getWidth()
        val h = bitmap.getHeight()

        val pix = IntArray(w * h)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)

        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r: IntArray? = IntArray(wh)
        val g: IntArray? = IntArray(wh)
        val b: IntArray? = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin: IntArray? = IntArray(max(w, h))

        var divsum = (div + 1) shr 1
        divsum *= divsum
        val dv: IntArray? = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv!![i] = (i / divsum)
            i++
        }

        yi = 0
        yw = yi

        val stack: Array<IntArray> = Array<IntArray?>(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int

        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            i = -radius
            while (i <= radius) {
                p = pix[yi + min(wm, max(i, 0))]
                sir = stack[i + radius]
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = (p and 0x0000ff)
                rbs = r1 - abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius

            x = 0
            while (x < w) {
                r!![yi] = dv!![rsum]
                g!![yi] = dv[gsum]
                b!![yi] = dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (y == 0) {
                    vmin!![x] = min(x + radius + 1, wm)
                }
                p = pix[yw + vmin!![x]]

                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = (p and 0x0000ff)

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[(stackpointer) % div]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi++
                x++
            }
            yw += w
            y++
        }
        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = max(0, yp) + x

                sir = stack[i + radius]

                sir[0] = r!![yi]
                sir[1] = g!![yi]
                sir[2] = b!![yi]

                rbs = r1 - abs(i)

                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs

                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }

                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {
                pix[yi] =
                    (-0x1000000 and pix[yi]) or (dv!![rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (x == 0) {
                    vmin!![y] = min(y + r1, hm) * w
                }
                p = x + vmin!![y]

                sir[0] = r!![p]
                sir[1] = g!![p]
                sir[2] = b!![p]

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi += w
                y++
            }
            x++
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h)

        return bitmap
    }
}
