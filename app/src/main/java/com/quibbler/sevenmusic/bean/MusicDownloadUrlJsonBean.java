package com.quibbler.sevenmusic.bean;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      MusicDownloadUrlJsonBean
 * Description:    在线音乐播放下载链接解析Json bean
 * Author:         zhaopeng
 * CreateDate:     2019/9/26 16:00
 */
public class MusicDownloadUrlJsonBean {

    private int code;

    private List<Data> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public class Data {
        private int id;
        private String url = null;
        private int size;
        private String type;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}

