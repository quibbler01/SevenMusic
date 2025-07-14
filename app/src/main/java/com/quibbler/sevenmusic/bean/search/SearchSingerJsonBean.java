package com.quibbler.sevenmusic.bean.search;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.bean.search
 * ClassName:      SearchSingerJsonBean
 * Description:    搜索歌手，信息解析
 * Author:         zhaopeng
 * CreateDate:     2019/9/29 18:48
 */
public class SearchSingerJsonBean {
    @SerializedName("result")
    private SearchSingerResult result;

    public SearchSingerResult getResult() {
        return result;
    }

    public void setResult(SearchSingerResult result) {
        this.result = result;
    }

    public class SearchSingerResult {
        @SerializedName("artists")
        List<SearchSingerArtists> artists;

        public List<SearchSingerArtists> getArtists() {
            return artists;
        }

        public void setArtists(List<SearchSingerArtists> artists) {
            this.artists = artists;
        }
    }

    public class SearchSingerArtists {
        @SerializedName("name")
        private String name;

        @SerializedName("picUrl")
        private String picUrl;

        @SerializedName("img1v1Url")
        private String img1v1Url;

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

        public String getImg1v1Url() {
            return img1v1Url;
        }

        public void setImg1v1Url(String img1v1Url) {
            this.img1v1Url = img1v1Url;
        }
    }

}
