package com.quibbler.sevenmusic.bean.search;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.bean.search
 * ClassName:      SearchSuggestionBean
 * Description:    搜索建议Json解析
 * Author:         zhaopeng
 * CreateDate:     2019/9/29 10:13
 */
public class SearchSuggestionBean {
    @SerializedName("result")
    private Result result;

    @SerializedName("code")
    private String code;

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
        @SerializedName("allMatch")
        private List<SearchSuggestion> allMatch;

        public List<SearchSuggestion> getAllMatch() {
            return allMatch;
        }

        public void setAllMatch(List<SearchSuggestion> allMatch) {
            this.allMatch = allMatch;
        }
    }

    public class SearchSuggestion {
        @SerializedName("keyword")
        private String keyword;

        @SerializedName("type")
        private String type;

        @SerializedName("alg")
        private String alg;

        @SerializedName("lastKeyword")
        private String lastKeyword;

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getAlg() {
            return alg;
        }

        public void setAlg(String alg) {
            this.alg = alg;
        }

        public String getLastKeyword() {
            return lastKeyword;
        }

        public void setLastKeyword(String lastKeyword) {
            this.lastKeyword = lastKeyword;
        }
    }
}
