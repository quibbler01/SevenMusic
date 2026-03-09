package com.quibbler.sevenmusic.bean.search

class SearchAlbumBean {
    var code: String? = null
    var result: Result? = null

    inner class Result {
        var albumCount: String? = null
        var albums: MutableList<Album?>? = null
    }

    inner class Album : SearchBean() {
        var name: String? = null
        var id: String? = null
        var blurPicUrl: String? = null
        var artist: Artist? = null
    }

    inner class Artist {
        var id: String? = null
        var name: String? = null
    }
}
