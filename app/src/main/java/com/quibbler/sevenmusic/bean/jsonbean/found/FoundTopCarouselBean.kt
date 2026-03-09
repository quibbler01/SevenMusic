package com.quibbler.sevenmusic.bean.jsonbean.found

import com.quibbler.sevenmusic.bean.MusicInfo

/**
 * 
 * Package:        com.quibbler.sevenmusic.bean.jsonbean.found
 * ClassName:      FoundTopCarouselBean
 * Description:    轮播内容解析的bean
 * Author:         yanwuyang
 * CreateDate:     2019/10/10 16:48
 */
class FoundTopCarouselBean {
    //banner图的url地址
    val pic: String? = null

    //bean的type为1即歌曲时，targetId为歌曲id
    val targetId: String? = null

    //type为1表示是歌曲
    val targetType: String? = null
    val typeTitle: String? = null
    val song: MusicInfo? = null
}
