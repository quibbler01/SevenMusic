package com.quibbler.sevenmusic.bean

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      MyCollectionsInfo
 * Description:    我的收藏bean数据
 * Author:         zhaopeng
 * CreateDate:     2019/9/20 14:59
 */
class MyCollectionsInfo {
    var id: Int = 0
    var title: String? = "暂无描述"
    var description: String? = "暂无描述"
    var kind: Int = 0

    constructor()

    constructor(id: Int) {
        this.id = id
    }
}
