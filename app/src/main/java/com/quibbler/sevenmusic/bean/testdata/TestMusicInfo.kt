package com.quibbler.sevenmusic.bean.testdata

import android.content.ContentValues
import android.net.Uri
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider

/**
 * Package:        com.quibbler.sevenmusic.bean.testdata
 * ClassName:      TestMusicInfo
 * Description:    数据插入数据库，提供数据给各个页面测试
 * Author:         zhaopeng
 * CreateDate:     2019/9/20 9:19
 */
object TestMusicInfo {
    var musicInfos: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
    var downloadUri: Uri =
        Uri.parse("content://" + MusicContentProvider.Companion.MUSIC_AUTHORITY + "/download")
    var playedUri: Uri =
        Uri.parse("content://" + MusicContentProvider.Companion.MUSIC_AUTHORITY + "/played")
    var favouriteUri: Uri =
        Uri.parse("content://" + MusicContentProvider.Companion.MUSIC_AUTHORITY + "/favourite")
    var collectionUri: Uri =
        Uri.parse("content://" + MusicContentProvider.Companion.MUSIC_AUTHORITY + "/collection")
    var songListUri: Uri =
        Uri.parse("content://" + MusicContentProvider.Companion.MUSIC_AUTHORITY + "/list")

    init {
        for (i in 0..4) {
            val musicInfo = MusicInfo()
            musicInfo.setMusicSongName("歌曲" + i + ".mp3")
            musicInfo.setSinger("歌手" + i)
            musicInfos.add(musicInfo)
            val values = ContentValues()
            values.put("id", i)
            values.put("name", musicInfo.getMusicSongName())
            values.put("singer", musicInfo.getSinger())
            values.put("is_download", 0)
            MusicApplication.Companion.getContext().getContentResolver().insert(downloadUri, values)
        }
        for (i in 6..10) {
            val musicInfo = MusicInfo()
            musicInfo.setMusicSongName("歌曲" + i + ".mp3")
            musicInfo.setSinger("歌手" + i)
            musicInfos.add(musicInfo)
            val values = ContentValues()
            values.put("id", i)
            values.put("name", musicInfo.getMusicSongName())
            values.put("singer", musicInfo.getSinger())
            values.put("is_download", 1)
            MusicApplication.Companion.getContext().getContentResolver().insert(downloadUri, values)
        }

        for (i in 12..16) {
            val musicInfo = MusicInfo()
            musicInfo.setMusicSongName("最近播放歌曲" + i + ".mp3")
            musicInfo.setSinger("歌手" + i)
            musicInfos.add(musicInfo)
            val values = ContentValues()
            values.put("id", i)
            values.put("name", musicInfo.getMusicSongName())
            values.put("singer", musicInfo.getSinger())
            values.put("last_played", System.currentTimeMillis())
            MusicApplication.Companion.getContext().getContentResolver().insert(playedUri, values)
        }
        for (i in 12..16) {
            val musicInfo = MusicInfo()
            musicInfo.setMusicSongName("我喜欢的歌曲" + i + ".mp3")
            musicInfo.setSinger("歌手" + i)
            musicInfos.add(musicInfo)
            val values = ContentValues()
            values.put("id", i)
            values.put("name", musicInfo.getMusicSongName())
            values.put("singer", musicInfo.getSinger())
            MusicApplication.Companion.getContext().getContentResolver()
                .insert(favouriteUri, values)
        }
        //插入收藏测试数据
        for (i in 12..16) {
            val values = ContentValues()
            values.put("id", i)
            values.put("title", "收藏歌曲" + i)
            values.put("description", "歌曲测试")
            values.put("kind", 0)
            MusicApplication.Companion.getContext().getContentResolver()
                .insert(collectionUri, values)
        }
        for (i in 18..22) {
            val values = ContentValues()
            values.put("id", i)
            values.put("title", "收藏歌手" + i)
            values.put("description", "歌手测试")
            values.put("kind", 1)
            MusicApplication.Companion.getContext().getContentResolver()
                .insert(collectionUri, values)
        }
        for (i in 24..25) {
            val values = ContentValues()
            values.put("id", i)
            values.put("title", "收藏专辑" + i)
            values.put("description", "专辑测试")
            values.put("kind", 2)
            MusicApplication.Companion.getContext().getContentResolver()
                .insert(collectionUri, values)
        }

        //歌单数据测试
        for (i in 18..19) {
            val values = ContentValues()
            values.put("name", "测试自建歌单 " + i)
            values.put("description", "自建歌单" + i)
            values.put("type", 0)
            values.put("number", i * 2)
            values.put("songs", "json")
            MusicApplication.Companion.getContext().getContentResolver().insert(songListUri, values)
        }
        for (i in 24..25) {
            val values = ContentValues()
            values.put("name", "收藏测试歌单 " + i)
            values.put("description", "收藏歌单" + i)
            values.put("type", 1)
            values.put("number", i * 2)
            values.put("songs", "json")
            MusicApplication.Companion.getContext().getContentResolver().insert(songListUri, values)
        }
    }
}
