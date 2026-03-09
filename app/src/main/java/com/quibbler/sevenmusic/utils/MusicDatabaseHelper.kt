package com.quibbler.sevenmusic.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      MusicDatabaseHelper
 * Description:    数据库帮助类 2.2 版
 * Author:         zhaopeng
 * CreateDate:     2019/9/21 20:22
 */
class MusicDatabaseHelper : SQLiteOpenHelper {
    private constructor(context: Context?, version: Int) : super(context, MUSIC_DB, null, version)

    private constructor(
        context: Context?,
        name: String?,
        factory: CursorFactory?,
        version: Int
    ) : super(context, name, factory, version)

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_MUSIC_TABLE)
        db.execSQL(CREATE_MUSIC_DOWNLOAD_TABLE)
        db.execSQL(CREATE_MUSIC_PLAYED_TABLE)
        db.execSQL(CREATE_MUSIC_FAVOURITE_TABLE)
        db.execSQL(CREATE_MUSIC_COLLECTION_TABLE)
        db.execSQL(CREATE_MUSIC_MV_TABLE)
        db.execSQL(CREATE_MUSIC_LIST_TABLE)
        db.execSQL(CREATE_PLAY_LIST_TABLE)
        //预设数据
        db.execSQL("INSERT INTO mv VALUES(10892907,\"我和我的祖国\",\"http://p4.music.126.net/dQGecKaFKeYCLihGd-dmZw==/109951164383180334.jpg\")")
        db.execSQL("INSERT INTO list VALUES(\"我的歌单\",\"默认歌单\",0,0,-1,\"1571713906139\",null,\"\")")
        //        db.execSQL("INSERT INTO list VALUES(\"经典老歌,久听不厌\",\"流年忧光影\",1,115,988690134,null,\"http://p1.music.126.net/VFd5cboNTbnYsWZ5DBn9bg==/18953381440004340.jpg\",null)");
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //not used
    }

    companion object {
        const val dbVersion: Int = 1

        const val MUSIC_DB: String = "music.db" //音乐记录数据库

        const val MUSIC_INFO: String = "music" //应用内音乐记录表
        const val MUSIC_DOWNLOAD: String = "download" //下载项表
        const val MUSIC_PLAYED: String = "played" //最近播放表
        const val MUSIC_FAVOURITE: String = "favourite" //我喜欢音乐表
        const val MUSIC_COLLECTION: String = "collection" //我的收藏
        const val MUSIC_MV_TABLE: String = "mv" //mv表
        const val MUSIC_LIST: String = "list" //歌单表
        const val PLAY_LIST: String = "playlist" //播放列表

        private val CREATE_MUSIC_TABLE = "create table music(" +
                "id text primary key  NOT NULL UNIQUE," +
                "name	text," +
                "singer	text," +
                "path	text," +
                "url	text," +
                "album	text)"

        private val CREATE_MUSIC_DOWNLOAD_TABLE = "create table download(" +
                "id text primary key  NOT NULL UNIQUE," +
                "name	text," +
                "singer	text," +
                "path	text," +
                "url	text," +
                "success 	integer default 0," +
                "is_download	integer)"

        private val CREATE_MUSIC_PLAYED_TABLE = "create table played(" +
                "id text primary key  NOT NULL UNIQUE," +
                "name	text," +
                "singer	text," +
                "path	text," +
                "url	text," +
                "album	text," +
                "last_played long)"

        private val CREATE_MUSIC_FAVOURITE_TABLE = "create table favourite(" +
                "id text primary key  NOT NULL UNIQUE," +
                "name	text," +
                "singer	text," +
                "path	text)"

        private val CREATE_MUSIC_COLLECTION_TABLE = "create table collection(" +
                "id text primary key  NOT NULL UNIQUE," +
                "title	text," +
                "description text," +
                "kind	integer)"

        private val CREATE_MUSIC_MV_TABLE = "create table mv(" +
                "id text primary key  NOT NULL UNIQUE," +
                "name	text," +
                "pictureurl	text)"

        private val CREATE_MUSIC_LIST_TABLE = "create table list(" +
                "name text primary key  NOT NULL UNIQUE," +
                "description	text," +
                "type integer," +
                "number	integer," +
                "id text NOT NULL," +
                "creator text," +
                "coverimgurl text," +
                "songs	text)"

        private val CREATE_PLAY_LIST_TABLE = "create table playlist(" +
                "id text primary key  NOT NULL UNIQUE," +
                "name	text," +
                "singer	text," +
                "path	text)"

        private var mDBHelper: MusicDatabaseHelper? = null

        /**
         * 只能采用懒汉模式。因为饿汉模式static初始化，不能获得Context对象
         * 
         * @return
         */
        @Synchronized
        fun getInstance(context: Context?): MusicDatabaseHelper {
            if (mDBHelper == null) {
                mDBHelper = MusicDatabaseHelper(context, MUSIC_DB, null, dbVersion)
            }
            return mDBHelper!!
        }
    }
}
