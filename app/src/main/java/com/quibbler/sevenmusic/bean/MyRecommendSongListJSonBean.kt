package com.quibbler.sevenmusic.bean;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      MyRecommendSongListJSonBean
 * Description:    我的页面，推荐若干个歌单，解析歌单数据
 * Author:         zhaopeng
 * CreateDate:     2019/9/24 10:14
 */
public class MyRecommendSongListJSonBean {
    List<MyRecommendSongList> playlists;

    public MyRecommendSongListJSonBean() {

    }

    public List<MyRecommendSongList> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(List<MyRecommendSongList> playlists) {
        this.playlists = playlists;
    }

    public class MyRecommendSongList {
        private String id;
        private String coverImgId;
        private String name;
        private String coverImgUrl;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return coverImgId;
        }

        public void setType(String coverImgId) {
            this.coverImgId = coverImgId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCoverImgUrl() {
            return coverImgUrl;
        }

        public void setCoverImgUrl(String coverImgUrl) {
            this.coverImgUrl = coverImgUrl;
        }
    }
}
