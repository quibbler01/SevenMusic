package com.quibbler.sevenmusic.bean.mv;

/**
  *
  * Package:        com.quibbler.sevenmusic.bean.mv
  * ClassName:      MvComment
  * Description:    mv评论
  * Author:         lishijun
  * CreateDate:     2019/9/25 19:48
 */
public class MvComment {

    //评论id
    private int mId;
    //评论内容
    private String mContent;
    //评论人名字
    private String mUserName;
    //评论人头像
    private String mUerHeadUrl;
    //评论日期
    private long mDate;
    //评论被点赞次数
    private int mLikeCount;

    public MvComment(int id, String content, String userName, String uerHeadUrl, long date, int likeCount) {
        mId = id;
        mContent = content;
        mUserName = userName;
        mUerHeadUrl = uerHeadUrl;
        mDate = date;
        mLikeCount = likeCount;
    }

    public int getId() {
        return mId;
    }

    public String getContent() {
        return mContent;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getUerHeadUrl() {
        return mUerHeadUrl;
    }

    public long getDate() {
        return mDate;
    }

    public int getLikeCount() {
        return mLikeCount;
    }
}
