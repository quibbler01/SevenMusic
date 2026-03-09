package com.quibbler.sevenmusic.bean

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      IsMusicMatch
 * Description:    用于搜索建立匹配索引
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 16:38
 */
class IsMusicMatch {
    var isMusicNameMatch: Boolean = false
    var isSingleNameMatch: Boolean = false
    var keyLength: Int = 0
    var musicNameStart: Int = 0
    var singleNameStart: Int = 0
}