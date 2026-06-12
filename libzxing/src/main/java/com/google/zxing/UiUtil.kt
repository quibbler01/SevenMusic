package com.google.zxing

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast

/**
 * Created by cuiqing on 2015/5/22.
 */
object UiUtil {
    /**
     * 弹出Toast,默认使用短显示
     */
    fun showToast(context: Context?, msg: String?) {
        showToast(context, msg, Toast.LENGTH_SHORT)
    }

    fun showToast(context: Context?, text: String?, duration: Int) {
        Toast.makeText(context, text, duration).show()
    }


    /**
     * 转换dp为px
     */
    fun dp2px(context: Context, dp: Int): Int {
        return (context.getResources().getDisplayMetrics().density * dp).toInt()
    }

    /**
     * 转换dp为px
     */
    fun dp2px(context: Context, dp: Float): Int {
        return (context.getResources().getDisplayMetrics().density * dp).toInt()
    }

    /**
     * 获取屏幕宽度
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
     * 获取屏幕高度
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
