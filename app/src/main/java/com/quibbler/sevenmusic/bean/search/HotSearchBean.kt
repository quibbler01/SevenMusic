package com.quibbler.sevenmusic.bean.search

import com.google.gson.annotations.SerializedName

/**
 * Package:        com.quibbler.sevenmusic.bean.search
 * ClassName:      HotSearchBean
 * Description:    热门搜索
 * Author:         zhaopeng
 * CreateDate:     2019/9/28 20:19
 */
class HotSearchBean {
    @SerializedName("code")
    var code: String? = null

    @SerializedName("data")
    var data: MutableList<Data?>? = null

    inner class Data {
        @SerializedName("searchWord")
        var searchWord: String? = null

        @SerializedName("score")
        var score: String? = null

        @SerializedName("content")
        var content: String? = null

        @SerializedName("source")
        var source: String? = null

        @SerializedName("iconType")
        var iconType: String? = null

        @SerializedName("iconUrl")
        var iconUrl: String? = null
    }
}
