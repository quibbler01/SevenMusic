package com.quibbler.sevenmusic.bean;

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      IsMusicMatch
 * Description:    用于搜索建立匹配索引
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 16:38
 */
public class IsMusicMatch {
    private boolean isMusicNameMatch = false;
    private boolean isSingleNameMatch = false;
    private int keyLength;
    private int musicNameStart;
    private int singleNameStart;

    public IsMusicMatch() {
    }

    public boolean isMusicNameMatch() {
        return isMusicNameMatch;
    }

    public void setMusicNameMatch(boolean musicNameMatch) {
        isMusicNameMatch = musicNameMatch;
    }

    public boolean isSingleNameMatch() {
        return isSingleNameMatch;
    }

    public void setSingleNameMatch(boolean singleNameMatch) {
        isSingleNameMatch = singleNameMatch;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public void setKeyLength(int keyLength) {
        this.keyLength = keyLength;
    }

    public int getMusicNameStart() {
        return musicNameStart;
    }

    public void setMusicNameStart(int musicNameStart) {
        this.musicNameStart = musicNameStart;
    }

    public int getSingleNameStart() {
        return singleNameStart;
    }

    public void setSingleNameStart(int singleNameStart) {
        this.singleNameStart = singleNameStart;
    }
}