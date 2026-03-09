package com.quibbler.sevenmusic.bean.mv

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      MvInfo
 * Description:    MV信息类
 * Author:         lishijun
 * CreateDate:     2019/9/19 21:22
 */
class MvInfo : Serializable {
    //mv id
    @SerializedName("id")
    var id: Int = 0

    //mv名字
    @SerializedName("name")
    var name: String? = null

    //mv歌手
    @SerializedName("artists")
    var artists: MutableList<Artist?>? = null

    //mv播放地址
    @SerializedName("brs")
    var mvUrl: MvUrl? = null

    var url: String? = null

    //mv播放次数
    @SerializedName("playCount")
    var playCount: Int = 0

    //mv编辑话语
    @SerializedName("briefDesc")
    var copyWriter: String? = null

    //mv缩略图
    @SerializedName("cover")
    var pictureUrl: String? = null

    constructor()
    constructor(
        id: Int,
        name: String?,
        artists: MutableList<Artist?>?,
        playCount: Int,
        copyWriter: String?,
        pictureUrl: String?
    ) {
        this.id = id
        this.name = name
        this.artists = artists
        this.playCount = playCount
        this.copyWriter = copyWriter
        this.pictureUrl = pictureUrl
    }

    override fun equals(obj: Any?): Boolean {
        if (obj is MvInfo) {
            val mv = obj
            return this.id == mv.id
        }
        return false
    }

    fun copy(mvInfo: MvInfo) {
        this.id = mvInfo.id
        this.name = mvInfo.name
        this.artists = mvInfo.artists
        this.url = mvInfo.url
        this.playCount = mvInfo.playCount
        this.copyWriter = mvInfo.copyWriter
        this.pictureUrl = mvInfo.pictureUrl
    }
}
