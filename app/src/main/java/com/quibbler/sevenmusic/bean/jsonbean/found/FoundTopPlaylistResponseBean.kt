package com.quibbler.sevenmusic.bean.jsonbean.found

/**
 * 
 * Package:        com.quibbler.sevenmusic.bean.jsonbean
 * ClassName:      FoundTopPlaylistResponseBean
 * Description:    网络请求“发现”页面中推荐歌单，返回的responseJson解析得到的Bean
 * Author:         yanwuyang
 * CreateDate:     2019/9/17 17:57
 */
class FoundTopPlaylistResponseBean {
    //返回的播放列表实体
    val playlists: MutableList<PlaylistInfo?>? = null

    //返回码
    val code: Int = 0
}
