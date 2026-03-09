package com.quibbler.sevenmusic.utils

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import androidx.annotation.WorkerThread
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quibbler.sevenmusic.Constant
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.bean.CustomMusicList
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      MusicDataBaseUtils
 * Description:    单曲加入自建歌单方法抽取出来，工具方法。必须在子线程执行@WorkerThread。已测试，闫午阳使用。
 * Author:         zhaopeng
 * CreateDate:     2019/10/22 22:15
 */
object MusicDatabaseUtils {
    private const val TAG = "MusicDatabaseUtils"

    var LIST_TYPE_CUSTOM: Int = 0 //自建歌单可以修改，type为0
    var LIST_TYPE_COLLECT: Int = 1 //收藏歌单无法修改，type为1

    @WorkerThread
    fun addToMusicList(listName: String?, musicInfo: MusicInfo): Boolean {
        val cursor: Cursor? = MusicApplication.Companion.getContext().getContentResolver().query(
            MusicContentProvider.Companion.SONGLIST_URL,
            null,
            "name = ?",
            arrayOf<String?>(listName),
            null
        )
        if (cursor == null || cursor.getCount() == 0) {
            return false
        }
        val gson = Gson()
        val contentValues = ContentValues()
        try {
            var musicInfoArrayList: java.util.ArrayList<MusicInfo>? = null
            cursor.moveToFirst()
            var songs = cursor.getString(cursor.getColumnIndex("songs"))
            if (songs == null || "" == songs) {
                musicInfoArrayList = java.util.ArrayList<MusicInfo>()
                musicInfoArrayList.add(musicInfo)
                songs = gson.toJson(musicInfoArrayList)
                contentValues.put("number", 1)
            } else {
                musicInfoArrayList = gson.fromJson<java.util.ArrayList<MusicInfo>?>(
                    songs,
                    object : TypeToken<MutableList<MusicInfo?>?>() {
                    }.getType()
                )
                for (mi in musicInfoArrayList) {
                    if (mi.getId() == musicInfo.getId()) {
                        Log.w(TAG, "already exist in this song list")
                        return false
                    }
                }
                musicInfoArrayList.add(0, musicInfo)
                //                musicInfoArrayList.add(musicInfo);
                songs = gson.toJson(musicInfoArrayList)
                contentValues.put("number", musicInfoArrayList.size)
            }
            contentValues.put("name", listName)
            contentValues.put("songs", songs)
            contentValues.put(
                "coverimgurl",
                Constant.SEVEN_MUSIC_IMAGE + "/" + musicInfo.getId()
            ) //不一定有封面，暂定默认。最好歌单封面是每次加入歌单最新的一首歌的封面
            MusicApplication.Companion.getContext().getContentResolver().update(
                MusicContentProvider.Companion.SONGLIST_URL,
                contentValues,
                "name = ?",
                arrayOf<String?>(listName)
            )
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            return false
        } finally {
            CloseResourceUtil.closeInputAndOutput(cursor)
        }
        return true
    }

    @WorkerThread
    fun getMusicList(listName: String?): java.util.ArrayList<MusicInfo?>? {
        var musicInfoArrayList: java.util.ArrayList<MusicInfo?>? = null

        val cursor: Cursor? = MusicApplication.Companion.getContext().getContentResolver().query(
            MusicContentProvider.Companion.SONGLIST_URL,
            null,
            "name = ?",
            arrayOf<String?>(listName),
            null
        )
        if (cursor == null || cursor.getCount() == 0) {
            return null
        } else {
            val gson = Gson()
            try {
                cursor.moveToFirst()
                val songs = cursor.getString(cursor.getColumnIndex("songs"))
                musicInfoArrayList = gson.fromJson<java.util.ArrayList<MusicInfo?>?>(
                    songs,
                    object : TypeToken<MutableList<MusicInfo?>?>() {
                    }.getType()
                )
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            } finally {
                CloseResourceUtil.closeInputAndOutput(cursor)
            }
        }
        return musicInfoArrayList
    }

    @get:WorkerThread
    val customMusicList: ArrayList<CustomMusicList?>?
        get() {
            val cursor: Cursor? =
                MusicApplication.Companion.getContext().getContentResolver().query(
                    MusicContentProvider.Companion.SONGLIST_URL,
                    null,
                    "type = ?",
                    arrayOf<String>("" + LIST_TYPE_CUSTOM),
                    null
                )
            if (cursor == null || cursor.getCount() == 0) {
                return null
            }
            val customMusicLists =
                java.util.ArrayList<CustomMusicList?>()
            try {
                if (cursor.moveToFirst()) {
                    do {
                        val name = cursor.getString(cursor.getColumnIndex("name"))
                        val coverUrl =
                            cursor.getString(cursor.getColumnIndex("coverimgurl"))
                        val customMusicList = CustomMusicList(name, coverUrl)
                        customMusicLists.add(customMusicList)
                    } while (cursor.moveToNext())
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            } finally {
                CloseResourceUtil.closeInputAndOutput(cursor)
            }
            return customMusicLists
        }
}