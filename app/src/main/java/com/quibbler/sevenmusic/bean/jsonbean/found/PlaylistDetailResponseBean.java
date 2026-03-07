package com.quibbler.sevenmusic.bean.jsonbean.found;

/**
  *
  * Package:        com.quibbler.sevenmusic.bean.jsonbean.found
  * ClassName:      PlaylistDetailResponseBean
  * Description:    歌单详细信息的responseBean
  * Author:         yanwuyang
  * CreateDate:     2019/9/24 10:45
 */
public class PlaylistDetailResponseBean {
    private String id;
    private PlaylistInfo playlist;
    private String code;

    public String getId() {
        return id;
    }

    public PlaylistInfo getPlaylist() {
        return playlist;
    }

    public String getCode() {
        return code;
    }
}
