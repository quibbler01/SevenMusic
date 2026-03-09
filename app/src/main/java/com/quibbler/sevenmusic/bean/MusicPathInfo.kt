package com.quibbler.sevenmusic.bean

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      MusicPathInfo
 * Description:    存储音乐目录信息，初始化 文件夹 页面
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 16:15
 */
class MusicPathInfo(mPathDetail: String, mMusicInfoLists: MutableList<MusicInfo?>) {
    var pathName: String? = null
    var pathDetail: String?
    var musicInfoLists: MutableList<MusicInfo?>

    init {
        this.pathDetail = mPathDetail
        this.musicInfoLists = mMusicInfoLists
        if (mPathDetail.contains(QQ_MUSIC)) {
            this.pathName = "QQ音乐"
        } else if (mPathDetail.contains(NETEASE)) {
            this.pathName = "网易云音乐"
        } else if (mPathDetail.contains(MUSIC)) {
            this.pathName = "音乐"
        } else if (mPathDetail == STORAGE) {
            this.pathName = "手机存储"
        } else if (mPathDetail.contains(SEVENMUSIC)) {
            this.pathName = "七音"
        } else {
            val index = mPathDetail.lastIndexOf("/")
            this.pathName = mPathDetail.substring(index + 1)
        }
    }

    fun add(musicInfo: MusicInfo?) {
        musicInfoLists.add(musicInfo)
    }

    fun addAll(list: MutableList<MusicInfo?>) {
        musicInfoLists.addAll(list)
    }

    fun updateData(list: MutableList<MusicInfo?>) {
        musicInfoLists.clear()
        musicInfoLists.addAll(list)
    }

    companion object {
        private const val QQ_MUSIC = "/storage/emulated/0/qqmusic/song" //QQ音乐
        private const val STORAGE = "/storage/emulated/0" //手机存储
        private const val MUSIC = "/storage/emulated/0/Music" //Music目录
        private const val NETEASE = "/storage/emulated/0/netease/cloudmusic/Music" //网易云音乐
        private const val SEVENMUSIC = "/storage/emulated/0/sevenMusic" //七音
    }
}
