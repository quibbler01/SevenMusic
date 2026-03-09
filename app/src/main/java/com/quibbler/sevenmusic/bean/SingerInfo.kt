package com.quibbler.sevenmusic.bean

import android.graphics.Bitmap

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      FoundSingerInfo
 * Description:    歌手信息类，初始化歌手
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 16:16
 */
class SingerInfo {
    var name: String?
    var songCount: Int = 0
    var songLists: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
    var isFemale: Boolean = true
    var picUrl: String? = null
    var id: String? = null
    private var mBitmap: Bitmap? = null

    constructor(name: String?) {
        this.name = name
    }

    constructor(name: String?, songLists: MutableList<MusicInfo?>) {
        this.name = name
        this.songLists.addAll(songLists)
    }

    fun add(musicInfo: MusicInfo?) {
        songLists.add(musicInfo)
    }

    /*
             更新数据集合方法
          */
    fun updateSongLists(songLists: MutableList<MusicInfo?>) {
        this.songLists.clear()
        this.songLists.addAll(songLists)
    }

    fun getmBitmap(): Bitmap? {
        return mBitmap
    }

    fun setmBitmap(mBitmap: Bitmap?) {
        this.mBitmap = mBitmap
    }
}
