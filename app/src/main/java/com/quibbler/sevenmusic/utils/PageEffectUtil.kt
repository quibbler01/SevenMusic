package com.quibbler.sevenmusic.utils

import android.util.Log
import android.widget.ScrollView

/**
 * 
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      PageEffectUtil
 * Description:    设置页面效果的util
 * Author:         lishijun
 * CreateDate:     2019/10/11 15:55
 */
object PageEffectUtil {
    private const val TAG = "PageEffectUtil"

    //设置scroll的回弹效果
    fun setScrollViewSpringback(scrollView: ScrollView?) {
        try {
            var cls: Class<*>? = null
            cls = Class.forName("android.widget.ScrollView")
            val mMethod_setSpringEffect =
                cls.getMethod("setSpringEffect", Boolean::class.javaPrimitiveType)
            val mMethod_setEdgeEffect =
                cls.getMethod("setEdgeEffect", Boolean::class.javaPrimitiveType)
            if (mMethod_setSpringEffect != null) {
                try {
                    mMethod_setSpringEffect.invoke(scrollView, true)
                } catch (e: Exception) {
                    Log.e(TAG, "setSpringEffect e: " + e.message)
                }
            }
            if (mMethod_setEdgeEffect != null) {
                try {
                    mMethod_setEdgeEffect.invoke(scrollView, false)
                } catch (e: Exception) {
                    Log.e(TAG, "setSpringEffect e: " + e.message)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "initMethod fail e: " + e.message)
            e.printStackTrace()
        }
    }
}
