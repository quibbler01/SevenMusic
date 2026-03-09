package com.quibbler.sevenmusic.bean

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      MusicURL
 * Description:    音乐接口API,一处修改，各处使用
 * Author:         zhaopeng
 * CreateDate:     2019/9/18 14:11
 */
object MusicURL {
    const val API: String = "http://114.116.128.229:3000"
    val API_HIGHQUALITY_SONGLIST: String = API + "/top/playlist/highquality?limit=6"
    val API_MUSIC_DOWNLOAD_URL: String = API + "/song/url?id="
    val API_GET_SONG_DETAIL_AND_IMAGE: String = API + "/song/detail?ids="

    const val MUSIC_ALBUM_URI: String = "content://media/external/audio/albums"
    val SEARCH_HOT: String = API + "/search/hot/detail"
    val SEARCH_SUGGESTION: String = API + "/search/suggest?type=mobile&keywords="
    const val SEARCH_SINGER: String = "http://114.116.128.229:3000/search?type=100&keywords="

    val SEARCH_MAIN_URL: String = API + "/search?type=%d&limit=%d&keywords=%s"

    const val API_MV: String = "http://114.116.128.229:3000/mv/url?id="

    //独家放送。用于发现页面轮播图
    val API_PRIVATE_CONTENT: String = API + "/banner?type=1"

    //最新歌单获取地址
    const val TOP_PLAYLIST_NUM: String = "9"
    val API_TOP_PLAYLIST_REQUEST_URL: String = API + "/top/playlist?limit=" + TOP_PLAYLIST_NUM

    //最新mv视频获取地址
    val API_TOP_MV_REQUEST_URL: String = API + "/mv/first?limit=4"
}
