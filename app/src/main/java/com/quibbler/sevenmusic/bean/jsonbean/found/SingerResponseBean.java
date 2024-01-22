package com.quibbler.sevenmusic.bean.jsonbean.found;

import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.bean.mv.Artist;

import java.util.List;

public class SingerResponseBean {
    private Artist artist;

    private List<MusicInfo> hotSongs;

    public List<MusicInfo> getHotSongs() {
        return hotSongs;
    }

    public Artist getArtist() {
        return artist;
    }
}
