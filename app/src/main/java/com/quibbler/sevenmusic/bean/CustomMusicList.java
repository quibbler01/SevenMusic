package com.quibbler.sevenmusic.bean;

/**
  *
  * Package:        com.quibbler.sevenmusic.bean
  * ClassName:      CustomMusicList
  * Description:    用户自建歌单
  * Author:         yanwuyang
  * CreateDate:     2019/11/13 16:41
 */
public class CustomMusicList {
    private String mName;
    private String mCoverImgUrl;

    public CustomMusicList(String name, String coverImgUrl) {
        mName = name;
        mCoverImgUrl = coverImgUrl;
    }

    public String getName() {
        return mName;
    }

    public String getCoverImgUrl() {
        return mCoverImgUrl;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setCoverImgUrl(String coverImgUrl) {
        this.mCoverImgUrl = coverImgUrl;
    }
}
