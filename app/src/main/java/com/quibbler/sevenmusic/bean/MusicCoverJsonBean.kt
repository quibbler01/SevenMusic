package com.quibbler.sevenmusic.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      MusicCoverJsonBean
 * Description:    获取歌曲封面
 * Author:         zhaopeng
 * CreateDate:     2019/9/27 15:43
 */
public class MusicCoverJsonBean {

    @SerializedName(value = "songs")
    private List<Song> songs;

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    /*
     *歌曲内容包裹
     */
    public class Song {
        @SerializedName(value = "al")
        private Al al;

        public Al getAl() {
            return al;
        }

        public void setAl(Al al) {
            this.al = al;
        }
    }

    /*
     *歌曲封面
     */
    public class Al {
        @SerializedName(value = "id")
        private String id;

        @SerializedName(value = "name")
        private String name;

        @SerializedName(value = "picUrl")
        private String picUrl;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public void setPicUrl(String picUrl) {
            this.picUrl = picUrl;
        }
    }
}
