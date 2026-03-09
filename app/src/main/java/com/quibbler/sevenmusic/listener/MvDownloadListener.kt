package com.quibbler.sevenmusic.listener;
/**
  *
  * Package:        com.quibbler.sevenmusic.listener
  * ClassName:      MvDownloadListener
  * Description:    mv下载监听
  * Author:         lishijun
  * CreateDate:     2019/9/24 11:04
 */
public interface MvDownloadListener {
    void onProgress(int progress);
    void onSuccess();
    void onFailed();
    void onPaused();
    void onCanceled();
}
