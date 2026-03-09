package com.quibbler.sevenmusic.bean.search

class SearchPlayListBean {
    var code: String? = null
    var result: Result? = null

    inner class Result {
        var playlistCount: String? = null
        var playlists: MutableList<PlayList?>? = null
    }

    inner class PlayList : SearchBean() {
        var id: String? = null
        var name: String? = null
        var coverImgUrl: String? = null
        var trackCount: String? = null
        var playCount: String? = null
    }
}
