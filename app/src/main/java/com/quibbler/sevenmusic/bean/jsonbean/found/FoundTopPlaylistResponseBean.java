package com.quibbler.sevenmusic.bean.jsonbean.found;

import java.util.List;

/**
  *
  * Package:        com.quibbler.sevenmusic.bean.jsonbean
  * ClassName:      FoundTopPlaylistResponseBean
  * Description:    网络请求“发现”页面中推荐歌单，返回的responseJson解析得到的Bean
  * Author:         yanwuyang
  * CreateDate:     2019/9/17 17:57
 */
public class FoundTopPlaylistResponseBean {
    //返回的播放列表实体
    private List<PlaylistInfo> playlists;
    //返回码
    private int code;

    public List<PlaylistInfo> getPlaylists() {
        return playlists;
    }

    public int getCode() {
        return code;
    }
}
