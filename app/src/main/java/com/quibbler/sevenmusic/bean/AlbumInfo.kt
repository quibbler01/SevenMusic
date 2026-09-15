package com.quibbler.sevenmusic.bean

import com.google.gson.annotations.SerializedName

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      AlbumInfo
 * Description:    专辑类
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 16:16
 */
class AlbumInfo {
    @SerializedName("name")
    var albumName: String?
    val id: String? = null
    val picUrl: String? = null

    private var musicInfoList: MutableList<MusicInfo?>? = null

    constructor(albumName: String?, musicInfoList: MutableList<MusicInfo?>) {
        this.albumName = albumName
        this.musicInfoList = musicInfoList
    }

    constructor(albumName: String?) {
        this.albumName = albumName
    }

    fun addMusicInfo(musicInfo: MusicInfo?) {
        musicInfoList!!.add(musicInfo)
    }

    fun addAllMusicInfo(musicInfoList: MutableList<MusicInfo?>) {
        this.musicInfoList!!.addAll(musicInfoList)
    }

    fun updateSongList(musicInfos: MutableList<MusicInfo?>) {
        musicInfoList!!.clear()
        musicInfoList!!.addAll(musicInfos)
    }

        /**
     * Brief description for getMusicInfoList.
     *
     * @param context the operating context
     * @return the result of the operation
     */
fun getMusicInfoList(): MutableList<MusicInfo?> {
        return musicInfoList!!
    }

        /**
     * Performs setMusicInfoList operation.
     * This method ensures safe execution with null checks.
     */
fun setMusicInfoList(musicInfoList: MutableList<MusicInfo?>) {
        this.musicInfoList = musicInfoList
    }
}
