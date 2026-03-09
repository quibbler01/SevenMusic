package com.quibbler.sevenmusic.bean.search

import com.google.gson.annotations.SerializedName

/**
 * Package:        com.quibbler.sevenmusic.bean.search
 * ClassName:      SearchSuggestionBean
 * Description:    搜索建议Json解析
 * Author:         zhaopeng
 * CreateDate:     2019/9/29 10:13
 */
class SearchSuggestionBean {
    @SerializedName("result")
    var result: Result? = null

    @SerializedName("code")
    var code: String? = null

    inner class Result {
        @SerializedName("allMatch")
        var allMatch: MutableList<SearchSuggestion?>? = null
    }

    inner class SearchSuggestion {
        @SerializedName("keyword")
        var keyword: String? = null

        @SerializedName("type")
        var type: String? = null

        @SerializedName("alg")
        var alg: String? = null

        @SerializedName("lastKeyword")
        var lastKeyword: String? = null
    }
}
