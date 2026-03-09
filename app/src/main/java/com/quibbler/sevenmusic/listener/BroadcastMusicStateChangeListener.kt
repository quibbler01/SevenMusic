package com.quibbler.sevenmusic.listener;

/**
  *
  * Package:        com.quibbler.sevenmusic.listener
  * ClassName:      BroadcastMusicStateChangeListener
  * Description:    音乐播放状态改变（开始播放、暂停、播放完毕），或出现其他情况（暂无版权、其他错误）时，需要作出响应的类，可以实现此接口。
  * Author:         yanwuyang
  * CreateDate:     2019/10/8 11:20
 */
public interface BroadcastMusicStateChangeListener extends BroadcastMusicPlayListener {
    void onNoCopyright();

    void onSomethingWrong();
}
