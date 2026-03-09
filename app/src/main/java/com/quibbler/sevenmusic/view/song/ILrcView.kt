package com.quibbler.sevenmusic.view.song

import com.quibbler.sevenmusic.bean.song.impl.LrcRow

/**
 * 
 * Package:        com.quibbler.sevenmusic.view.song
 * ClassName:      ILrcView
 * Description:    歌词view接口
 * Author:         lishijun
 * CreateDate:     2019/9/28 15:11
 */
interface ILrcView {
    val isScrolling: Boolean

    /**
     * 设置要展示的歌词行集合
     */
    fun setLrc(lrcRows: MutableList<LrcRow?>?)

    /**
     * 音乐播放的时候调用该方法滚动歌词，高亮正在播放的那句歌词，cb表示是不是手动调用
     */
    fun seekLrcToTime(time: Long, cb: Boolean)

    fun seekLrc(position: Int, cb: Boolean)
}
