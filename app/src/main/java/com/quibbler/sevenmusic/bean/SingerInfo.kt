package com.quibbler.sevenmusic.bean;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      FoundSingerInfo
 * Description:    歌手信息类，初始化歌手
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 16:16
 */
public class SingerInfo {
    private String name;
    private int songCount;
    private List<MusicInfo> songLists = new ArrayList<>();
    private boolean female = true;
    private String picUrl;
    private String id;
    private Bitmap mBitmap = null;

    public SingerInfo(String name) {
        this.name = name;
    }

    public SingerInfo(String name, List<MusicInfo> songLists) {
        this.name = name;
        this.songLists.addAll(songLists);
    }

    public void add(MusicInfo musicInfo) {
        songLists.add(musicInfo);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSongCount() {
        return songCount;
    }

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    public List<MusicInfo> getSongLists() {
        return songLists;
    }

    public void setSongLists(List<MusicInfo> songLists) {
        this.songLists = songLists;
    }

    public String getId() {
        return id;
    }

    /*
            更新数据集合方法
         */
    public void updateSongLists(List<MusicInfo> songLists) {
        this.songLists.clear();
        this.songLists.addAll(songLists);
    }

    public boolean isFemale() {
        return female;
    }

    public void setFemale(boolean female) {
        this.female = female;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Bitmap getmBitmap() {
        return mBitmap;
    }

    public void setmBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }
}
