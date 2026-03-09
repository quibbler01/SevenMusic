package com.quibbler.sevenmusic.bean.mv;

import com.google.gson.annotations.SerializedName;
import com.quibbler.sevenmusic.bean.mv.Artist;

import java.io.Serializable;
import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      MvInfo
 * Description:    MV信息类
 * Author:         lishijun
 * CreateDate:     2019/9/19 21:22
 */
public class MvInfo implements Serializable {

    //mv id
    @SerializedName("id")
    private int mId;
    //mv名字
    @SerializedName("name")
    private String mName;
    //mv歌手
    @SerializedName("artists")
    private List<Artist> mArtists;
    //mv播放地址
    @SerializedName("brs")
    private MvUrl mMvUrl;

    private String mUrl;
    //mv播放次数
    @SerializedName("playCount")
    private int mPlayCount;
    //mv编辑话语
    @SerializedName("briefDesc")
    private String mCopyWriter;
    //mv缩略图
    @SerializedName("cover")
    private String mPictureUrl;

    public MvInfo() {

    }
    public MvInfo(int id, String name, List<Artist> artists, int playCount, String copyWriter, String pictureUrl) {
        mId = id;
        mName = name;
        mArtists = artists;
        mPlayCount = playCount;
        mCopyWriter = copyWriter;
        mPictureUrl = pictureUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MvInfo) {
            MvInfo mv = (MvInfo) obj;
            return mId == mv.getId();
        }
        return false;
    }

    public void copy(MvInfo mvInfo){
        mId = mvInfo.getId();
        mName = mvInfo.getName();
        mArtists = mvInfo.getArtists();
        mUrl = mvInfo.getUrl();
        mPlayCount = mvInfo.getPlayCount();
        mCopyWriter = mvInfo.getCopyWriter();
        mPictureUrl = mvInfo.getPictureUrl();
    }

    public MvUrl getMvUrl() {
        return mMvUrl;
    }

    public void setMvUrl(MvUrl mvUrl) {
        mMvUrl = mvUrl;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public List<Artist> getArtists() {
        return mArtists;
    }

    public void setArtists(List<Artist> artists) {
        mArtists = artists;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public int getPlayCount() {
        return mPlayCount;
    }

    public void setPlayCount(int playCount) {
        mPlayCount = playCount;
    }

    public String getCopyWriter() {
        return mCopyWriter;
    }

    public void setCopyWriter(String copyWriter) {
        mCopyWriter = copyWriter;
    }

    public String getPictureUrl() {
        return mPictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        mPictureUrl = pictureUrl;
    }
}
