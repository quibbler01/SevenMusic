package com.quibbler.sevenmusic.bean.search

class SearchSongBean {
    var code: String? = null
    var result: Result? = null

    inner class Result {
        var songCount: String? = null
        var songs: MutableList<Song?>? = null
    }

    inner class Song : SearchBean() {
        var id: String? = null
        var name: String? = null
        var artists: MutableList<Artist?>? = null
    }

    inner class Artist {
        var id: String? = null
        var name: String? = null
    }
}
