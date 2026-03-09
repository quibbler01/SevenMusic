package com.quibbler.sevenmusic.utils

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.quibbler.sevenmusic.MusicApplication

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      ResUtil
 * Description:    获取应用资源的封装
 * Author:         11103876
 * CreateDate:     2019/10/7 15:31
 */
object ResUtil {
    val resources: Resources?
        /**
         * 描述：获取Resource对象
         * 
         * @return Returns a Resources getInstance for your application's Package.
         */
        get() = MusicApplication.Companion.getContext().getResources()

    /**
     * 描述：获取Drawable资源
     * 
     * @param resId
     * @return
     */
    fun getDrawable(resId: Int): Drawable? {
        return ContextCompat.getDrawable(MusicApplication.Companion.getContext(), resId)
    }

    /**
     * 描述：获取字符串资源
     * 
     * @param resId
     * @return
     */
    fun getString(resId: Int): String {
        return resources.getString(resId)
    }

    /**
     * 描述：获取color资源
     * 
     * @param resId
     * @return
     */
    fun getColor(resId: Int): Int {
        return ContextCompat.getColor(MusicApplication.Companion.getContext(), resId)
    }

    /**
     * 描述：获取dimens资源
     * 
     * @param resId
     * @return
     */
    fun getDimens(resId: Int): Float {
        return resources.getDimension(resId)
    }

    /**
     * 描述：获取字符串数组资源
     * 
     * @param resId
     * @return
     */
    fun getStringArray(resId: Int): Array<String?> {
        return resources.getStringArray(resId)
    }

    /**
     * 描述：根据颜色值计算颜色
     * 
     * @param color color值
     * @param alpha alpha值
     * @return 最终的颜色
     */
    fun calculateStatusColor(color: Int, alpha: Int): Int {
        val a = 1 - alpha / 255f
        var red = color shr 16 and 0xff
        var green = color shr 8 and 0xff
        var blue = color and 0xff
        red = (red * a + 0.5).toInt()
        green = (green * a + 0.5).toInt()
        blue = (blue * a + 0.5).toInt()
        return 0xff shl 24 or (red shl 16) or (green shl 8) or blue
    }
}
