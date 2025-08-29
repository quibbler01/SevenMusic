package com.quibbler.sevenmusic.bean;

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      MyCollectionsInfo
 * Description:    我的收藏bean数据
 * Author:         zhaopeng
 * CreateDate:     2019/9/20 14:59
 */
public class MyCollectionsInfo {
    private int id;
    private String title = "暂无描述";
    private String description = "暂无描述";
    private int kind = 0;

    public MyCollectionsInfo() {

    }

    public MyCollectionsInfo(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }
}
