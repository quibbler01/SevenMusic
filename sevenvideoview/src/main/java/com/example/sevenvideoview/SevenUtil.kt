package com.example.sevenvideoview

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import java.util.Formatter
import java.util.Locale

/**
 * 工具类.
 */
object SevenUtil {
    /**
     * Get activity from context object
     * 
     * @param context something
     * @return object of Activity or null if it is not Activity
     */
    fun scanForActivity(context: Context?): Activity? {
        if (context == null) return null
        if (context is Activity) {
            return context
        } else if (context is ContextWrapper) {
            return scanForActivity(context.getBaseContext())
        }
        return null
    }

    /**
     * Get AppCompatActivity from context
     * 
     * @param context
     * @return AppCompatActivity if it's not null
     */
    private fun getAppCompActivity(context: Context?): AppCompatActivity? {
        if (context == null) return null
        if (context is AppCompatActivity) {
            return context
        } else if (context is ContextThemeWrapper) {
            return getAppCompActivity(context.getBaseContext())
        }
        return null
    }

    fun showActionBar(context: Context?) {
        val ab = getAppCompActivity(context)!!.getSupportActionBar()
        if (ab != null) {
            ab.setShowHideAnimationEnabled(false)
            ab.show()
        }
        scanForActivity(context)!!
            .getWindow()
            .clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    fun hideActionBar(context: Context?) {
        val ab = getAppCompActivity(context)!!.getSupportActionBar()
        if (ab != null) {
            ab.setShowHideAnimationEnabled(false)
            ab.hide()
        }
        scanForActivity(context)!!
            .getWindow()
            .setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
    }

    /**
     * 获取屏幕宽度
     * 
     * @param context
     * @return width of the screen.
     */
    fun getScreenWidth(context: Context): Int {
        return context.getResources().getDisplayMetrics().widthPixels
    }

    /**
     * 获取屏幕高度
     * 
     * @param context
     * @return heiht of the screen.
     */
    fun getScreenHeight(context: Context): Int {
        return context.getResources().getDisplayMetrics().heightPixels
    }

    /**
     * dp转px
     * 
     * @param context
     * @param dpVal   dp value
     * @return px value
     */
    fun dp2px(context: Context, dpVal: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dpVal,
            context.getResources().getDisplayMetrics()
        ).toInt()
    }

    /**
     * 将毫秒数格式化为"##:##"的时间
     * 
     * @param milliseconds 毫秒数
     * @return ##:##
     */
    fun formatTime(milliseconds: Long): String {
        if (milliseconds <= 0 || milliseconds >= 24 * 60 * 60 * 1000) {
            return "00:00"
        }
        val totalSeconds = milliseconds / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        val stringBuilder = StringBuilder()
        val mFormatter = Formatter(stringBuilder, Locale.getDefault())
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    /**
     * 保存播放位置，以便下次播放时接着上次的位置继续播放.
     * 
     * @param context
     * @param url     视频链接url
     */
    fun savePlayPosition(context: Context, url: String?, position: Long) {
        context.getSharedPreferences(
            "NICE_VIDEO_PALYER_PLAY_POSITION",
            Context.MODE_PRIVATE
        )
            .edit()
            .putLong(url, position)
            .apply()
    }

    /**
     * 取出上次保存的播放位置
     * 
     * @param context
     * @param url     视频链接url
     * @return 上次保存的播放位置
     */
    fun getSavedPlayPosition(context: Context, url: String?): Long {
        return context.getSharedPreferences(
            "NICE_VIDEO_PALYER_PLAY_POSITION",
            Context.MODE_PRIVATE
        )
            .getLong(url, 0)
    }
}
