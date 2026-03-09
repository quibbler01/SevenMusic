package com.quibbler.sevenmusic.bean.mv

/**
 * 
 * Package:        com.quibbler.sevenmusic.bean.mv
 * ClassName:      MvComment
 * Description:    mv评论
 * Author:         lishijun
 * CreateDate:     2019/9/25 19:48
 */
class MvComment(//评论id
    val id: Int, //评论内容
    val content: String?, userName: String?, uerHeadUrl: String?, date: Long, likeCount: Int
) {
    //评论人名字
    val userName: String?

    //评论人头像
    val uerHeadUrl: String?

    //评论日期
    val date: Long

    //评论被点赞次数
    val likeCount: Int

    init {
        this.content = content
        this.userName = userName
        this.uerHeadUrl = uerHeadUrl
        this.date = date
        this.likeCount = likeCount
    }
}
