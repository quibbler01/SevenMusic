package com.quibbler.sevenmusic.bean.search

import com.google.gson.annotations.SerializedName

/**
 * Package:        com.quibbler.sevenmusic.bean.search
 * ClassName:      SearchSingerJsonBean
 * Description:    搜索歌手，信息解析
 * Author:         zhaopeng
 * CreateDate:     2019/9/29 18:48
 */
class SearchSingerJsonBean {
    @SerializedName("result")
    var result: SearchSingerResult? = null

    inner class SearchSingerResult {
        @SerializedName("artists")
        var artists: MutableList<SearchSingerArtists?>? = null
    }

    inner class SearchSingerArtists {
        @SerializedName("name")
        var name: String? = null

        @SerializedName("picUrl")
        var picUrl: String? = null

        @SerializedName("img1v1Url")
        var img1v1Url: String? = null
    }
}
