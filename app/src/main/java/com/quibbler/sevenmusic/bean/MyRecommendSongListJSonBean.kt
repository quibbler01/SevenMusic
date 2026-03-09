package com.quibbler.sevenmusic.bean

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      MyRecommendSongListJSonBean
 * Description:    我的页面，推荐若干个歌单，解析歌单数据
 * Author:         zhaopeng
 * CreateDate:     2019/9/24 10:14
 */
class MyRecommendSongListJSonBean {
    var playlists: MutableList<MyRecommendSongList?>? = null

    inner class MyRecommendSongList {
        var id: String? = null
        var type: String? = null
        var name: String? = null
        var coverImgUrl: String? = null
    }
}
