package com.quibbler.sevenmusic.utils

import android.content.Context
import android.util.Log

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      DecodeMusicImageUtils
 * Description:    音乐专辑封面解析工具类调用入口
 * Author:         zhaopeng
 * CreateDate:     2019/9/28 9:21
 */
object DecodeMusicImageUtils {
    fun with(context: Context): DecodeMusicManager {
        Log.d("DecodeMusicImageUtils", "with() called")
        return DecodeMusicManager(context)
    }
}
