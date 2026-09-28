package com.quibbler.sevenmusic.listener

/**
 * Package:        com.quibbler.sevenmusic.listener
 * ClassName:      MusicDownloadListener
 * Description:    下载监听器接口，更新下载进度和监听下载状态
 * Author:         zhaopeng
 * CreateDate:     2019/9/16 22:51
 */
interface MusicDownloadListener {
        /**
     * Performs onProgress operation.
     * This method ensures safe execution with null checks.
     */
fun onProgress(progress: Int, name: String?)

        /**
     * Handles isSuccess logic with proper error handling.
     */
fun isSuccess(result: Boolean) //    public void onFaield();
    //
    //    public void onCanceled();
    //
    //    public void onPasued();
}
