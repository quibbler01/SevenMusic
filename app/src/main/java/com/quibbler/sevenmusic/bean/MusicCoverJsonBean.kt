package com.quibbler.sevenmusic.bean

import com.google.gson.annotations.SerializedName

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      MusicCoverJsonBean
 * Description:    获取歌曲封面
 * Author:         zhaopeng
 * CreateDate:     2019/9/27 15:43
 */
class MusicCoverJsonBean {
    @SerializedName(value = "songs")
    var songs: MutableList<Song?>? = null

    /*
      *歌曲内容包裹
      */
    inner class Song {
        @SerializedName(value = "al")
        var al: Al? = null
    }

    /*
     *歌曲封面
     */
    inner class Al {
        @SerializedName(value = "id")
        var id: String? = null

        @SerializedName(value = "name")
        var name: String? = null

        @SerializedName(value = "picUrl")
        var picUrl: String? = null
    }
}
