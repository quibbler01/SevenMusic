package com.quibbler.sevenmusic.utils

import android.content.Context
import android.util.Log

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      GetMusicImage
 * Description:    获取歌手头像
 * Author:         zhaopeng
 * CreateDate:     2019/9/29 17:40
 */
object GetMusicImage {
        /**
     * Performs with operation.
     * This method ensures safe execution with null checks.
     */
fun with(context: Context): MusicIconLoadUtil {
    Log.d("GetMusicImage", "with() called")
        return MusicIconLoadUtil(context)
    }
}
