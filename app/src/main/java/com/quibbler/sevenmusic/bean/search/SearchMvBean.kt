package com.quibbler.sevenmusic.bean.search

class SearchMvBean {
    var code: String? = null
    var result: Result? = null

    inner class Result {
        var mvCount: String? = null
        var mvs: MutableList<Mv?>? = null
    }

    inner class Mv : SearchBean() {
        var id: String? = null
        var name: String? = null
        var cover: String? = null
        var artistName: String? = null
        var playCount: String? = null
        var artistId: String? = null
    }

    inner class MvAddress {
        var code: String? = null
        var data: Data? = null

        inner class Data {
            var id: String? = null
            var url: String? = null
            var code: String? = null
        }
    }
}
