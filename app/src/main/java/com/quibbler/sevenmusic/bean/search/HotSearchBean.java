package com.quibbler.sevenmusic.bean.search;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.bean.search
 * ClassName:      HotSearchBean
 * Description:    热门搜索
 * Author:         zhaopeng
 * CreateDate:     2019/9/28 20:19
 */
public class HotSearchBean {
    @SerializedName("code")
    private String code;

    @SerializedName("data")
    private List<Data> data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public class Data {
        @SerializedName("searchWord")
        private String searchWord;

        @SerializedName("score")
        private String score;

        @SerializedName("content")
        private String content;

        @SerializedName("source")
        private String source;

        @SerializedName("iconType")
        private String iconType;

        @SerializedName("iconUrl")
        private String iconUrl = null;

        public String getSearchWord() {
            return searchWord;
        }

        public void setSearchWord(String searchWord) {
            this.searchWord = searchWord;
        }

        public String getScore() {
            return score;
        }

        public void setScore(String score) {
            this.score = score;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getIconType() {
            return iconType;
        }

        public void setIconType(String iconType) {
            this.iconType = iconType;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }
    }
}
