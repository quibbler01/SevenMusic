package com.quibbler.sevenmusic.bean.mv;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
  *
  * Package:        com.quibbler.sevenmusic.bean
  * ClassName:      Artist
  * Description:    歌手类
  * Author:         lishijun
  * CreateDate:     2019/9/19 21:44
 */
public class Artist implements Serializable {

    //歌手id
    @SerializedName("id")
    int mId;

    //歌手名字
    @SerializedName("name")
    String mName;

    //歌手图片url
    String picUrl;
    //歌手简介
    String briefDesc;
    //歌手歌曲数
    private int musicSize;
    //歌手mv数
    private int mvSize;

    public Artist(int id, String name) {
        mId = id;
        mName = name;
    }
    public Artist() {

    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public String getBriefDesc() {
        return briefDesc;
    }
}
