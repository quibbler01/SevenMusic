package com.quibbler.sevenmusic.bean.search;

import java.util.List;

public class SearchMvBean {
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
        private String mvCount;
        private List<Mv> mvs;

        public String getMvCount() {
            return mvCount;
        }

        public void setMvCount(String mvCount) {
            this.mvCount = mvCount;
        }

        public List<Mv> getMvs() {
            return mvs;
        }

        public void setMvs(List<Mv> mvs) {
            this.mvs = mvs;
        }
    }

    public class Mv extends SearchBean {
        private String id;
        private String name;
        private String cover;
        private String artistName;
        private String playCount;
        private String artistId;

        public String getArtistId() {
            return artistId;
        }

        public void setArtistId(String artistId) {
            this.artistId = artistId;
        }

        public String getPlayCount() {
            return playCount;
        }

        public void setPlayCount(String playCount) {
            this.playCount = playCount;
        }

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

        public String getCover() {
            return cover;
        }

        public void setCover(String cover) {
            this.cover = cover;
        }

        public String getArtistName() {
            return artistName;
        }

        public void setArtistName(String artistName) {
            this.artistName = artistName;
        }
    }

    public class MvAddress {
        private String code;
        private Data data;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        public class Data {
            private String id;
            private String url;
            private String code;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getCode() {
                return code;
            }

            public void setCode(String code) {
                this.code = code;
            }
        }
    }

}
