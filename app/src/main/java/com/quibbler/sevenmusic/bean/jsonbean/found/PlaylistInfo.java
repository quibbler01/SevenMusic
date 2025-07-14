package com.quibbler.sevenmusic.bean.jsonbean.found;

import androidx.annotation.Nullable;

import com.quibbler.sevenmusic.bean.Creator;
import com.quibbler.sevenmusic.bean.MusicInfo;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.bean.jsonbean
 * ClassName:      PlaylistInfo
 * Description:    “发现”页面歌单的Bean
 * Author:         yanwuyang
 * CreateDate:     2019/9/17 16:50
 */
public class PlaylistInfo {
    private String id;
    //歌单封面图片下载url，http开头
    private String coverImgUrl;
    //歌单名
    private String name;
    //歌单内歌曲list
    private List<MusicInfo> tracks;
    //歌单包含的歌曲数
    private int trackCount;
    //歌单创建者
    private Creator creator;
    //歌单描述
    private String description;

    public String getId() {
        return id;
    }

    public String getCoverImgUrl() {
        return coverImgUrl;
    }

    public String getName() {
        return name;
    }

    public List<MusicInfo> getTracks() {
        return tracks;
    }

    public Creator getCreator() {
        return creator;
    }

    public String getDescription() {
        return description;
    }

    public int getTrackCount() {
        return trackCount;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCoverImgUrl(String coverImgUrl) {
        this.coverImgUrl = coverImgUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTracks(List<MusicInfo> tracks) {
        this.tracks = tracks;
    }

    public void setTrackCount(int trackCount) {
        this.trackCount = trackCount;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof PlaylistInfo) {
            PlaylistInfo playlistInfo = (PlaylistInfo) obj;
            return playlistInfo.getId().equals(id);
        }
        return false;
    }
}
