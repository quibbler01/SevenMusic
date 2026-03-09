package com.quibbler.sevenmusic.bean.mv

import java.io.Serializable

/**
 * 
 * Package:        com.quibbler.sevenmusic.bean.mv
 * ClassName:      MvMusicInfo
 * Description:    mv中用到的歌曲信息
 * Author:         lishijun
 * CreateDate:     2019/9/25 15:56
 */
class MvMusicInfo(id: Int, name: String?, pictureUrl: String?, artistList: MutableList<Artist?>?) :
    Serializable {
    //音乐id
    val id: Int

    //音乐名字
    val name: String?

    //专辑封面
    var pictureUrl: String?

    //音乐歌手
    val artistList: MutableList<Artist?>?

    //是否可用
    var isCanUse: Boolean = false

    //歌词
    var lyric: String? = null

    init {
        this.id = id
        this.name = name
        this.pictureUrl = pictureUrl
        this.artistList = artistList
    }
}
