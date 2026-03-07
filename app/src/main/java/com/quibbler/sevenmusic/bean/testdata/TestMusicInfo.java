package com.quibbler.sevenmusic.bean.testdata;

import android.content.ContentValues;
import android.net.Uri;

import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.bean.testdata
 * ClassName:      TestMusicInfo
 * Description:    数据插入数据库，提供数据给各个页面测试
 * Author:         zhaopeng
 * CreateDate:     2019/9/20 9:19
 */
public class TestMusicInfo {
    public static List<MusicInfo> musicInfos = new ArrayList<>();
    public static Uri downloadUri = Uri.parse("content://" + MusicContentProvider.MUSIC_AUTHORITY + "/download");
    public static Uri playedUri = Uri.parse("content://" + MusicContentProvider.MUSIC_AUTHORITY + "/played");
    public static Uri favouriteUri = Uri.parse("content://" + MusicContentProvider.MUSIC_AUTHORITY + "/favourite");
    public static Uri collectionUri = Uri.parse("content://" + MusicContentProvider.MUSIC_AUTHORITY + "/collection");
    public static Uri songListUri = Uri.parse("content://" + MusicContentProvider.MUSIC_AUTHORITY + "/list");

    static {
        for (int i = 0; i < 5; ++i) {
            MusicInfo musicInfo = new MusicInfo();
            musicInfo.setMusicSongName("歌曲" + i + ".mp3");
            musicInfo.setSinger("歌手" + i);
            musicInfos.add(musicInfo);
            ContentValues values = new ContentValues();
            values.put("id", i);
            values.put("name", musicInfo.getMusicSongName());
            values.put("singer", musicInfo.getSinger());
            values.put("is_download", 0);
            MusicApplication.getContext().getContentResolver().insert(downloadUri, values);
        }
        for (int i = 6; i < 11; ++i) {
            MusicInfo musicInfo = new MusicInfo();
            musicInfo.setMusicSongName("歌曲" + i + ".mp3");
            musicInfo.setSinger("歌手" + i);
            musicInfos.add(musicInfo);
            ContentValues values = new ContentValues();
            values.put("id", i);
            values.put("name", musicInfo.getMusicSongName());
            values.put("singer", musicInfo.getSinger());
            values.put("is_download", 1);
            MusicApplication.getContext().getContentResolver().insert(downloadUri, values);
        }

        for (int i = 12; i < 17; ++i) {
            MusicInfo musicInfo = new MusicInfo();
            musicInfo.setMusicSongName("最近播放歌曲" + i + ".mp3");
            musicInfo.setSinger("歌手" + i);
            musicInfos.add(musicInfo);
            ContentValues values = new ContentValues();
            values.put("id", i);
            values.put("name", musicInfo.getMusicSongName());
            values.put("singer", musicInfo.getSinger());
            values.put("last_played", System.currentTimeMillis());
            MusicApplication.getContext().getContentResolver().insert(playedUri, values);
        }
        for (int i = 12; i < 17; ++i) {
            MusicInfo musicInfo = new MusicInfo();
            musicInfo.setMusicSongName("我喜欢的歌曲" + i + ".mp3");
            musicInfo.setSinger("歌手" + i);
            musicInfos.add(musicInfo);
            ContentValues values = new ContentValues();
            values.put("id", i);
            values.put("name", musicInfo.getMusicSongName());
            values.put("singer", musicInfo.getSinger());
            MusicApplication.getContext().getContentResolver().insert(favouriteUri, values);
        }
        //插入收藏测试数据
        for (int i = 12; i < 17; ++i) {
            ContentValues values = new ContentValues();
            values.put("id", i);
            values.put("title", "收藏歌曲" + i);
            values.put("description", "歌曲测试");
            values.put("kind", 0);
            MusicApplication.getContext().getContentResolver().insert(collectionUri, values);
        }
        for (int i = 18; i < 23; ++i) {
            ContentValues values = new ContentValues();
            values.put("id", i);
            values.put("title", "收藏歌手" + i);
            values.put("description", "歌手测试");
            values.put("kind", 1);
            MusicApplication.getContext().getContentResolver().insert(collectionUri, values);
        }
        for (int i = 24; i < 26; ++i) {
            ContentValues values = new ContentValues();
            values.put("id", i);
            values.put("title", "收藏专辑" + i);
            values.put("description", "专辑测试");
            values.put("kind", 2);
            MusicApplication.getContext().getContentResolver().insert(collectionUri, values);
        }

        //歌单数据测试
        for (int i = 18; i < 20; ++i) {
            ContentValues values = new ContentValues();
            values.put("name", "测试自建歌单 " + i);
            values.put("description", "自建歌单" + i);
            values.put("type", 0);
            values.put("number", i * 2);
            values.put("songs", "json");
            MusicApplication.getContext().getContentResolver().insert(songListUri, values);
        }
        for (int i = 24; i < 26; ++i) {
            ContentValues values = new ContentValues();
            values.put("name", "收藏测试歌单 " + i);
            values.put("description", "收藏歌单" + i);
            values.put("type", 1);
            values.put("number", i * 2);
            values.put("songs", "json");
            MusicApplication.getContext().getContentResolver().insert(songListUri, values);
        }
    }

}
