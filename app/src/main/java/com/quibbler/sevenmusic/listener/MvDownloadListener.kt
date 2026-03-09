package com.quibbler.sevenmusic.listener

/**
 * 
 * Package:        com.quibbler.sevenmusic.listener
 * ClassName:      MvDownloadListener
 * Description:    mv下载监听
 * Author:         lishijun
 * CreateDate:     2019/9/24 11:04
 */
interface MvDownloadListener {
    fun onProgress(progress: Int)
    fun onSuccess()
    fun onFailed()
    fun onPaused()
    fun onCanceled()
}
