package com.quibbler.sevenmusic.bean.search;

import java.util.List;

public class SearchArtistsBean {
    private String code;
    private Result result;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public class Result {
        private String artistCount;
        private List<Artist> artists;

        public String getArtistCount() {
            return artistCount;
        }

        public void setArtistCount(String artistCount) {
            this.artistCount = artistCount;
        }

        public List<Artist> getArtists() {
            return artists;
        }

        public void setArtists(List<Artist> artists) {
            this.artists = artists;
        }
    }

    public class Artist extends SearchBean {
        private String name;
        private String id;
        private String picUrl;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public void setPicUrl(String picUrl) {
            this.picUrl = picUrl;
        }
    }
}
