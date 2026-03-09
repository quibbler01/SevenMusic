package com.quibbler.sevenmusic.interfaces

/**
 * Package:        com.quibbler.sevenmusic.interfaces
 * ClassName:      MusicStateManager
 * Description:    音乐播放状态变化时，通知所有需要对此进行响应的接口
 * Author:         yanwuyang
 * CreateDate:     2019/10/8 10:33
 */
class MusicStateManager private constructor() {
    private val mStateChangeList: MutableList<MusicStateChangeInterface> =
        ArrayList<MusicStateChangeInterface>()

    /**
     * 将接口添加到manager中进行管理
     * 
     * @param musicStateChangeInterface
     */
    fun addToManager(musicStateChangeInterface: MusicStateChangeInterface?) {
        mStateChangeList.add(musicStateChangeInterface!!)
    }

    /**
     * 将接口对象从manager中移除
     * 
     * @param musicStateChangeInterface
     */
    fun removeFromManager(musicStateChangeInterface: MusicStateChangeInterface?) {
        mStateChangeList.remove(musicStateChangeInterface)
    }

    /**
     * 播放音乐时，通知所有接口
     * 
     * @param id 音乐的id
     */
    fun onMusicPlay(id: String?) {
        for (musicStateChangeInterface in mStateChangeList) {
            musicStateChangeInterface.onMusicPlay(id)
        }
    }

    /**
     * 暂停播放时，通知所有接口
     * 
     * @param id 音乐的id
     */
    fun onMusicPause(id: String?) {
        for (musicStateChangeInterface in mStateChangeList) {
            musicStateChangeInterface.onMusicPause(id)
        }
    }

    /**
     * 歌曲无版权时，通知所有接口
     * 
     * @param id 音乐的id
     */
    fun onNoCopyright(id: String?) {
        for (musicStateChangeInterface in mStateChangeList) {
            musicStateChangeInterface.onNoCopyright(id)
        }
    }


    /**
     * 发生其他错误，通知所有接口
     * 
     * @param id 音乐的id
     */
    fun onSomethingWrong(id: String?) {
        for (musicStateChangeInterface in mStateChangeList) {
            musicStateChangeInterface.onSomethingWrong(id)
        }
    }

    companion object {
        @Volatile
        private var sMusicStateManager: MusicStateManager? = null
        val instance: MusicStateManager?
            get() {
                if (sMusicStateManager == null) {
                    synchronized(MusicStateManager::class.java) {
                        if (sMusicStateManager == null) {
                            sMusicStateManager = MusicStateManager()
                        }
                    }
                }
                return sMusicStateManager
            }
    }
}
