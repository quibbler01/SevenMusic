package com.quibbler.sevenmusic.bean.search

class SearchArtistsBean {
    var code: String? = null
    var result: Result? = null

    inner class Result {
        var artistCount: String? = null
        var artists: MutableList<Artist?>? = null
    }

    inner class Artist : SearchBean() {
        var name: String? = null
        var id: String? = null
        var picUrl: String? = null
    }
}
