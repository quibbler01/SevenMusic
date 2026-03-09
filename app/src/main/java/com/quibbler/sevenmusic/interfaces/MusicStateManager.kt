package com.quibbler.sevenmusic.interfaces;

import java.util.ArrayList;
import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.interfaces
 * ClassName:      MusicStateManager
 * Description:    音乐播放状态变化时，通知所有需要对此进行响应的接口
 * Author:         yanwuyang
 * CreateDate:     2019/10/8 10:33
 */
public class MusicStateManager {
    private static volatile MusicStateManager sMusicStateManager;
    private List<MusicStateChangeInterface> mStateChangeList = new ArrayList<>();

    private MusicStateManager() {

    }

    public static MusicStateManager getInstance() {
        if (sMusicStateManager == null) {
            synchronized (MusicStateManager.class) {
                if (sMusicStateManager == null) {
                    sMusicStateManager = new MusicStateManager();
                }
            }
        }
        return sMusicStateManager;
    }

    /**
     * 将接口添加到manager中进行管理
     *
     * @param musicStateChangeInterface
     */
    public void addToManager(MusicStateChangeInterface musicStateChangeInterface) {
        mStateChangeList.add(musicStateChangeInterface);
    }

    /**
     * 将接口对象从manager中移除
     *
     * @param musicStateChangeInterface
     */
    public void removeFromManager(MusicStateChangeInterface musicStateChangeInterface) {
        mStateChangeList.remove(musicStateChangeInterface);
    }

    /**
     * 播放音乐时，通知所有接口
     *
     * @param id 音乐的id
     */
    public void onMusicPlay(String id) {
        for (MusicStateChangeInterface musicStateChangeInterface : mStateChangeList) {
            musicStateChangeInterface.onMusicPlay(id);
        }
    }

    /**
     * 暂停播放时，通知所有接口
     *
     * @param id 音乐的id
     */
    public void onMusicPause(String id) {
        for (MusicStateChangeInterface musicStateChangeInterface : mStateChangeList) {
            musicStateChangeInterface.onMusicPause(id);
        }
    }

    /**
     * 歌曲无版权时，通知所有接口
     *
     * @param id 音乐的id
     */
    public void onNoCopyright(String id) {
        for (MusicStateChangeInterface musicStateChangeInterface : mStateChangeList) {
            musicStateChangeInterface.onNoCopyright(id);
        }
    }


    /**
     * 发生其他错误，通知所有接口
     *
     * @param id 音乐的id
     */
    public void onSomethingWrong(String id) {
        for (MusicStateChangeInterface musicStateChangeInterface : mStateChangeList) {
            musicStateChangeInterface.onSomethingWrong(id);
        }
    }
}
