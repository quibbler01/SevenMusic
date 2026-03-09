package com.quibbler.sevenmusic.utils

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      UiUtil
 * Description:    Ui帮助类
 * Author:         11103876
 * CreateDate:     2019/10/16 20:28
 */
object UiUtil {
    /**
     * 描述：弹出Toast,默认使用短显示
     */
    fun showToast(context: Context, msg: String?) {
        showToast(context, msg, Toast.LENGTH_SHORT)
    }

    fun showToast(context: Context, text: String?, duration: Int) {
        Toast.makeText(context, text, duration).show()
    }


    /**
     * 描述：转换dp为px
     */
    fun dp2px(context: Context, dp: Int): Int {
        return (context.getResources().getDisplayMetrics().density * dp).toInt()
    }

    /**
     * 描述：转换dp为px
     */
    fun dp2px(context: Context, dp: Float): Int {
        return (context.getResources().getDisplayMetrics().density * dp).toInt()
    }

    /**
     * 描述：获取屏幕宽度
     */
    fun getScreenWidth(context: Context): Int {
        val orientation = context.getResources().getConfiguration().orientation
        val width: Int
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            width = context.getResources().getDisplayMetrics().heightPixels
        } else {
            width = context.getResources().getDisplayMetrics().widthPixels
        }
        return width
    }

    /**
     * 描述：获取屏幕高度
     */
    fun getScreenHeight(context: Context): Int {
        val orientation = context.getResources().getConfiguration().orientation
        val height: Int
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            height = context.getResources().getDisplayMetrics().widthPixels
        } else {
            height = context.getResources().getDisplayMetrics().heightPixels
        }
        return height
    }
}
