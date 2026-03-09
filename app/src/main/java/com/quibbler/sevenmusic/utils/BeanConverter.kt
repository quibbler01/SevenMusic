package com.quibbler.sevenmusic.utils

import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.bean.SingerInfo
import com.quibbler.sevenmusic.bean.mv.Artist
import com.quibbler.sevenmusic.bean.mv.MvInfo
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      BeanConverter
 * Description:    Bean之间互相转换的工具类
 * Author:         yanwuyang
 * CreateDate:     2019/9/25 16:48
 */
object BeanConverter {
    fun convertMvInfo2MvInfo(MvInfo: MvInfo?): MvInfo? {
        if (MvInfo == null) {
            return null
        }

        val mvInfo = MvInfo(
            MvInfo.getId(),
            MvInfo.getName(),
            MvInfo.getArtists(),
            MvInfo.getPlayCount(),
            MvInfo.getCopyWriter(),
            MvInfo.getPictureUrl()
        )
        mvInfo.setUrl(MvInfo.getUrl())
        return mvInfo
    }

    fun convertSingerInfo2Artist(singerInfo: SingerInfo?): Artist? {
        if (singerInfo == null) {
            return null
        }

        val artist = Artist(singerInfo.getId().toInt(), singerInfo.getName())
        return artist
    }

    fun convertMusicInfo2MvMusicInfo(musicInfo: MusicInfo): MvMusicInfo {
        val id = musicInfo.getId().toInt()
        val name = musicInfo.getMusicSongName()
        val picUrl = musicInfo.getAlbumPicUrl()
        val artists: MutableList<Artist?> = ArrayList<Artist?>()
        if (musicInfo.getAr() != null) {
            for (singerInfo in musicInfo.getAr()) {
                artists.add(convertSingerInfo2Artist(singerInfo))
            }
        }
        return MvMusicInfo(id, name, picUrl, artists)
    }
}
