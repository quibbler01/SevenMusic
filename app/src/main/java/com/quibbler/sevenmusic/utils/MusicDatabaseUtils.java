package com.quibbler.sevenmusic.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.WorkerThread;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.bean.CustomMusicList;
import com.quibbler.sevenmusic.bean.MusicInfo;

import java.util.ArrayList;
import java.util.List;

import static com.quibbler.sevenmusic.Constant.SEVEN_MUSIC_IMAGE;
import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.SONGLIST_URL;

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      MusicDataBaseUtils
 * Description:    单曲加入自建歌单方法抽取出来，工具方法。必须在子线程执行@WorkerThread。已测试，闫午阳使用。
 * Author:         zhaopeng
 * CreateDate:     2019/10/22 22:15
 */
public class MusicDatabaseUtils {
    private static final String TAG = "MusicDatabaseUtils";

    public static int LIST_TYPE_CUSTOM = 0;  //自建歌单可以修改，type为0
    public static int LIST_TYPE_COLLECT = 1;  //收藏歌单无法修改，type为1

    @WorkerThread
    public static boolean addToMusicList(String listName, MusicInfo musicInfo) {
        Cursor cursor = MusicApplication.getContext().getContentResolver().query(SONGLIST_URL, null, "name = ?", new String[]{listName}, null);
        if (cursor == null || cursor.getCount() == 0) {
            return false;
        }
        Gson gson = new Gson();
        ContentValues contentValues = new ContentValues();
        try {
            ArrayList<MusicInfo> musicInfoArrayList = null;
            cursor.moveToFirst();
            String songs = cursor.getString(cursor.getColumnIndex("songs"));
            if (songs == null || "".equals(songs)) {
                musicInfoArrayList = new ArrayList<>();
                musicInfoArrayList.add(musicInfo);
                songs = gson.toJson(musicInfoArrayList);
                contentValues.put("number", 1);
            } else {
                musicInfoArrayList = gson.fromJson(songs, new TypeToken<List<MusicInfo>>() {
                }.getType());
                for (MusicInfo mi : musicInfoArrayList) {
                    if (mi.getId().equals(musicInfo.getId())) {
                        Log.w(TAG, "already exist in this song list");
                        return false;
                    }
                }
                musicInfoArrayList.add(0, musicInfo);
//                musicInfoArrayList.add(musicInfo);
                songs = gson.toJson(musicInfoArrayList);
                contentValues.put("number", musicInfoArrayList.size());
            }
            contentValues.put("name", listName);
            contentValues.put("songs", songs);
            contentValues.put("coverimgurl", SEVEN_MUSIC_IMAGE + "/" + musicInfo.getId());                               //不一定有封面，暂定默认。最好歌单封面是每次加入歌单最新的一首歌的封面
            MusicApplication.getContext().getContentResolver().update(SONGLIST_URL, contentValues, "name = ?", new String[]{listName});
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        } finally {
            CloseResourceUtil.closeInputAndOutput(cursor);
        }
        return true;
    }

    @WorkerThread
    public static ArrayList<MusicInfo> getMusicList(String listName) {
        ArrayList<MusicInfo> musicInfoArrayList = null;

        Cursor cursor = MusicApplication.getContext().getContentResolver().query(SONGLIST_URL, null, "name = ?", new String[]{listName}, null);
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        } else {
            Gson gson = new Gson();
            try {
                cursor.moveToFirst();
                String songs = cursor.getString(cursor.getColumnIndex("songs"));
                musicInfoArrayList = gson.fromJson(songs, new TypeToken<List<MusicInfo>>() {
                }.getType());
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            } finally {
                CloseResourceUtil.closeInputAndOutput(cursor);
            }
        }
        return musicInfoArrayList;
    }

    @WorkerThread
    public static ArrayList<CustomMusicList> getCustomMusicList() {
        Cursor cursor = MusicApplication.getContext().getContentResolver().query(SONGLIST_URL, null, "type = ?", new String[]{"" + LIST_TYPE_CUSTOM}, null);
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        }
        ArrayList<CustomMusicList> customMusicLists = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    String coverUrl = cursor.getString(cursor.getColumnIndex("coverimgurl"));
                    CustomMusicList customMusicList = new CustomMusicList(name, coverUrl);
                    customMusicLists.add(customMusicList);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            CloseResourceUtil.closeInputAndOutput(cursor);
        }
        return customMusicLists;
    }

}