package com.quibbler.sevenmusic.bean.jsonbean.found;

import java.util.List;

/**
  *
  * Package:        com.quibbler.sevenmusic.bean.jsonbean
  * ClassName:      FoundPlaylistLibBean
  * Description:    歌单库按类型获取到的response的bean
  * Author:         yanwuyang
  * CreateDate:     2019/9/20 10:52
 */
public class FoundPlaylistLibBean {
    private List<PlaylistInfo> playlists;

    public List<PlaylistInfo> getPlaylists() {
        return playlists;
    }
}
