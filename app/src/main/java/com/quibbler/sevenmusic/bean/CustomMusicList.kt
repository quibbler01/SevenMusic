package com.quibbler.sevenmusic.bean

/**
 * 
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      CustomMusicList
 * Description:    用户自建歌单
 * Author:         yanwuyang
 * CreateDate:     2019/11/13 16:41
 */
class CustomMusicList(name: String?, coverImgUrl: String?) {
    var name: String?
    var coverImgUrl: String?

    init {
        this.name = name
        this.coverImgUrl = coverImgUrl
    }
}
