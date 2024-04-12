package com.quibbler.sevenmusic.interfaces;

/**
 * Package:        com.quibbler.sevenmusic.interfaces
 * ClassName:      MusicStateChangeInterface
 * Description:    音乐播放状态改变（开始播放、暂停、播放完毕），或出现其他情况（暂无版权、其他错误）时，需要作出响应的类，可以实现此接口。同时，需要通过MusicStateManager.addToManager，removeFromManager方法管控。
 * Author:         yanwuyang
 * CreateDate:     2019/10/8 10:10
 */
public interface MusicStateChangeInterface {
    //开始播放音乐
    void onMusicPlay(String musicInfoId);

    //暂停播放
    void onMusicPause(String musicInfoId);

    //暂无版权
    void onNoCopyright(String musicInfoId);

    //其他错误
    void onSomethingWrong(String musicInfoId);
}
