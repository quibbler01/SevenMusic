package com.quibbler.sevenmusic.bean.mv

import com.google.gson.annotations.SerializedName
import java.io.Serializable


/**
 * 
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      Artist
 * Description:    歌手类
 * Author:         lishijun
 * CreateDate:     2019/9/19 21:44
 */
class Artist : Serializable {
    //歌手id
    @SerializedName("id")
    var id: Int = 0

    //歌手名字
    @SerializedName("name")
    var name: String? = null

    //歌手图片url
    var picUrl: String? = null

    //歌手简介
    var briefDesc: String? = null

    //歌手歌曲数
    private val musicSize = 0

    //歌手mv数
    private val mvSize = 0

    constructor(id: Int, name: String?) {
        this.id = id
        this.name = name
    }

    constructor()
}
