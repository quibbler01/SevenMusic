package com.quibbler.sevenmusic.bean.jsonbean.found

import com.quibbler.sevenmusic.bean.mv.MvInfo

/**
 * 
 * Package:        com.quibbler.sevenmusic.bean.jsonbean
 * ClassName:      FoundTopMvResponseBean
 * Description:    网络请求“发现”页面中推荐mv视频，返回的responseJson解析得到的Bean
 * Author:         yanwuyang
 * CreateDate:     2019/9/18 20:41
 */
class FoundTopMvResponseBean {
    //返回的mv视频实体
    val data: MutableList<MvInfo?>? = null

    //返回码
    val code: Int = 0
}
