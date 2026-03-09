package com.quibbler.sevenmusic.bean

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      MusicDownloadUrlJsonBean
 * Description:    在线音乐播放下载链接解析Json bean
 * Author:         zhaopeng
 * CreateDate:     2019/9/26 16:00
 */
class MusicDownloadUrlJsonBean {
    var code: Int = 0

    var data: MutableList<Data?>? = null

    inner class Data {
        var id: Int = 0
        var url: String? = null
        var size: Int = 0
        var type: String? = null
    }
}

