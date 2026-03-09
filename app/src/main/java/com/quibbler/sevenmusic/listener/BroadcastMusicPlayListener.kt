package com.quibbler.sevenmusic.listener;

/**
 * Package:        com.quibbler.sevenmusic.listener
 * ClassName:      BroadcastMusicPlayListener
 * Description:    接收广播，处理具体事务
 * Author:         zhaoopeng
 * CreateDate:     2019/9/27 18:41
 */
public interface BroadcastMusicPlayListener extends BroadcastListener {
    public void handBroadcast();

    public void onMusicPlay();

    public void onMusicPause();
}
