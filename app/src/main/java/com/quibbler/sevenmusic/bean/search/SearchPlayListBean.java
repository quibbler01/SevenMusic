package com.quibbler.sevenmusic.bean.search;

import java.util.List;

public class SearchPlayListBean {
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
        private String playlistCount;
        private List<PlayList> playlists;

        public String getPlaylistCount() {
            return playlistCount;
        }

        public void setPlaylistCount(String playlistCount) {
            this.playlistCount = playlistCount;
        }

        public List<PlayList> getPlaylists() {
            return playlists;
        }

        public void setPlaylists(List<PlayList> playlists) {
            this.playlists = playlists;
        }
    }

    public class PlayList extends SearchBean {
        private String id;
        private String name;
        private String coverImgUrl;
        private String trackCount;
        private String playCount;

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

        public String getCoverImgUrl() {
            return coverImgUrl;
        }

        public void setCoverImgUrl(String coverImgUrl) {
            this.coverImgUrl = coverImgUrl;
        }

        public String getTrackCount() {
            return trackCount;
        }

        public void setTrackCount(String trackCount) {
            this.trackCount = trackCount;
        }

        public String getPlayCount() {
            return playCount;
        }

        public void setPlayCount(String playCount) {
            this.playCount = playCount;
        }
    }

}
