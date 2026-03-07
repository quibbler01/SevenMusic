package com.quibbler.sevenmusic.bean;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      MusicPathInfo
 * Description:    存储音乐目录信息，初始化 文件夹 页面
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 16:15
 */
public class MusicPathInfo {
    private final static String QQ_MUSIC = "/storage/emulated/0/qqmusic/song";                  //QQ音乐
    private final static String STORAGE = "/storage/emulated/0";                                //手机存储
    private final static String MUSIC = "/storage/emulated/0/Music";                            //Music目录
    private final static String NETEASE = "/storage/emulated/0/netease/cloudmusic/Music";       //网易云音乐
    private final static String SEVENMUSIC = "/storage/emulated/0/sevenMusic";                  //七音

    private String mPathName;
    private String mPathDetail;
    private List<MusicInfo> mMusicInfoLists;

    public MusicPathInfo(String mPathDetail, List<MusicInfo> mMusicInfoLists) {
        this.mPathDetail = mPathDetail;
        this.mMusicInfoLists = mMusicInfoLists;
        if (mPathDetail.contains(QQ_MUSIC)) {
            mPathName = "QQ音乐";
        } else if (mPathDetail.contains(NETEASE)) {
            mPathName = "网易云音乐";
        } else if (mPathDetail.contains(MUSIC)) {
            mPathName = "音乐";
        } else if (mPathDetail.equals(STORAGE)) {
            mPathName = "手机存储";
        } else if (mPathDetail.contains(SEVENMUSIC)) {
            mPathName = "七音";
        } else {
            int index = mPathDetail.lastIndexOf("/");
            mPathName = mPathDetail.substring(index + 1);
        }
    }

    public void add(MusicInfo musicInfo) {
        mMusicInfoLists.add(musicInfo);
    }

    public void addAll(List<MusicInfo> list) {
        mMusicInfoLists.addAll(list);
    }

    public void updateData(List<MusicInfo> list) {
        mMusicInfoLists.clear();
        mMusicInfoLists.addAll(list);
    }

    public String getPathName() {
        return mPathName;
    }

    public void setPathName(String mPathName) {
        this.mPathName = mPathName;
    }

    public String getPathDetail() {
        return mPathDetail;
    }

    public void setPathDetail(String mPathDetail) {
        this.mPathDetail = mPathDetail;
    }

    public List<MusicInfo> getMusicInfoLists() {
        return mMusicInfoLists;
    }

    public void setMusicInfoLists(List<MusicInfo> mMusicInfoLists) {
        this.mMusicInfoLists = mMusicInfoLists;
    }
}
