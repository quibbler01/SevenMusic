package com.quibbler.sevenmusic.bean;

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      MusicURL
 * Description:    音乐接口API,一处修改，各处使用
 * Author:         zhaopeng
 * CreateDate:     2019/9/18 14:11
 */
public class MusicURL {
    public static final String API = "http://114.116.128.229:3000";
    public static final String API_HIGHQUALITY_SONGLIST = API + "/top/playlist/highquality?limit=6";
    public static final String API_MUSIC_DOWNLOAD_URL = API + "/song/url?id=";
    public static final String API_GET_SONG_DETAIL_AND_IMAGE = API + "/song/detail?ids=";

    public static final String MUSIC_ALBUM_URI = "content://media/external/audio/albums";
    public static final String SEARCH_HOT = API + "/search/hot/detail";
    public static final String SEARCH_SUGGESTION = API + "/search/suggest?type=mobile&keywords=";
    public static final String SEARCH_SINGER = "http://114.116.128.229:3000/search?type=100&keywords=";

    public static final String SEARCH_MAIN_URL = API + "/search?type=%d&limit=%d&keywords=%s";

    public static final String API_MV = "http://114.116.128.229:3000/mv/url?id=";

    //独家放送。用于发现页面轮播图
    public static final String API_PRIVATE_CONTENT = API + "/banner?type=1";
    //最新歌单获取地址
    public static final String TOP_PLAYLIST_NUM = "9";
    public static final String API_TOP_PLAYLIST_REQUEST_URL = API + "/top/playlist?limit=" + TOP_PLAYLIST_NUM;
    //最新mv视频获取地址
    public static final String API_TOP_MV_REQUEST_URL = API + "/mv/first?limit=4";

}
