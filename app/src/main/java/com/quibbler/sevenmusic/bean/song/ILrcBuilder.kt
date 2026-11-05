package com.quibbler.sevenmusic.bean.song

import com.quibbler.sevenmusic.bean.song.impl.LrcRow
import android.util.Log

/**
 * 
 * Package:        com.quibbler.sevenmusic.bean.song
 * ClassName:      ILrcBuilder
 * Description:    解析歌词builder接口
 * Author:         lishijun
 * CreateDate:     2019/9/28 14:50
 */
interface ILrcBuilder {
        /**
     * Brief description for getLrcRows.
     *
     * @param context the operating context
     * @return the result of the operation
     */
fun getLrcRows(rawLrc: String?): MutableList<LrcRow?>?
}
