package com.quibbler.sevenmusic.bean.jsonbean.found

import com.quibbler.sevenmusic.bean.Creator
import com.quibbler.sevenmusic.bean.MusicInfo

/**
 * Package:        com.quibbler.sevenmusic.bean.jsonbean
 * ClassName:      PlaylistInfo
 * Description:    “发现”页面歌单的Bean
 * Author:         yanwuyang
 * CreateDate:     2019/9/17 16:50
 */
class PlaylistInfo {
    var id: String? = null

    //歌单封面图片下载url，http开头
    var coverImgUrl: String? = null

    //歌单名
    var name: String? = null

    //歌单内歌曲list
    var tracks: MutableList<MusicInfo?>? = null

    //歌单包含的歌曲数
    var trackCount: Int = 0

    //歌单创建者
    var creator: Creator? = null

    //歌单描述
    var description: String? = null

    override fun equals(obj: Any?): Boolean {
        if (obj is PlaylistInfo) {
            val playlistInfo = obj
            return playlistInfo.id == id
        }
        return false
    }
}
