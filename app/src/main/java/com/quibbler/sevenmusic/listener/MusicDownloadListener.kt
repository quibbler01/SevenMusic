package com.quibbler.sevenmusic.listener;
/**
 * Package:        com.quibbler.sevenmusic.listener
 * ClassName:      MusicDownloadListener
 * Description:    下载监听器接口，更新下载进度和监听下载状态
 * Author:         zhaopeng
 * CreateDate:     2019/9/16 22:51
 */
public interface MusicDownloadListener {
    public void onProgress(int progress, String name);

    public void isSuccess(boolean result);

//    public void onFaield();
//
//    public void onCanceled();
//
//    public void onPasued();
}
