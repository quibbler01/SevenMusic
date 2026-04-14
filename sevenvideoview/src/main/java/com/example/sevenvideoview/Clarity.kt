package com.example.sevenvideoview

/**
 * 清晰度
 */
class Clarity(// 清晰度等级
    var grade: String?, // 270P、480P、720P、1080P、4K ...
    var p: String?, // 视频链接地址
    var videoUrl: String?
) {
    init {
        this.videoUrl = videoUrl
    }
}