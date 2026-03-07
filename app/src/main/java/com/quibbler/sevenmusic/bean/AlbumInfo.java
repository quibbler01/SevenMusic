package com.quibbler.sevenmusic.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      AlbumInfo
 * Description:    专辑类
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 16:16
 */
public class AlbumInfo {
    @SerializedName("name")
    private String albumName;
    private String id;
    private String picUrl;

    private List<MusicInfo> musicInfoList;

    public AlbumInfo(String albumName, List<MusicInfo> musicInfoList) {
        this.albumName = albumName;
        this.musicInfoList = musicInfoList;
    }

    public AlbumInfo(String albumName) {
        this.albumName = albumName;
    }

    public void addMusicInfo(MusicInfo musicInfo) {
        musicInfoList.add(musicInfo);
    }

    public void addAllMusicInfo(List<MusicInfo> musicInfoList) {
        this.musicInfoList.addAll(musicInfoList);
    }

    public void updateSongList(List<MusicInfo> musicInfos) {
        musicInfoList.clear();
        musicInfoList.addAll(musicInfos);
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public List<MusicInfo> getMusicInfoList() {
        return musicInfoList;
    }

    public void setMusicInfoList(List<MusicInfo> musicInfoList) {
        this.musicInfoList = musicInfoList;
    }

    public String getId() {
        return id;
    }

    public String getPicUrl() {
        return picUrl;
    }
}
