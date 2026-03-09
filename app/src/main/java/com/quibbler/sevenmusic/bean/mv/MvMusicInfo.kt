package com.quibbler.sevenmusic.bean.mv;

import java.io.Serializable;
import java.util.List;

/**
  *
  * Package:        com.quibbler.sevenmusic.bean.mv
  * ClassName:      MvMusicInfo
  * Description:    mv中用到的歌曲信息
  * Author:         lishijun
  * CreateDate:     2019/9/25 15:56
 */
public class MvMusicInfo implements Serializable {

    //音乐id
    private int mId;
    //音乐名字
    private String mName;
    //专辑封面
    private String mPictureUrl;
    //音乐歌手
    private List<Artist> mArtistList;
    //是否可用
    private boolean mCanUse = false;
    //歌词
    private String mLyric;

    public MvMusicInfo(int id, String name, String pictureUrl, List<Artist> artistList) {
        mId = id;
        mName = name;
        mPictureUrl = pictureUrl;
        mArtistList = artistList;
    }

    public void setPictureUrl(String pictureUrl) {
        mPictureUrl = pictureUrl;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getPictureUrl() {
        return mPictureUrl;
    }

    public List<Artist> getArtistList() {
        return mArtistList;
    }

    public boolean isCanUse() {
        return mCanUse;
    }

    public void setCanUse(boolean canUse) {
        mCanUse = canUse;
    }

    public String getLyric() {
        return mLyric;
    }

    public void setLyric(String lyric) {
        mLyric = lyric;
    }
}
