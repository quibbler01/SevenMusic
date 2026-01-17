package com.quibbler.sevenmusic.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      MusicDatabaseHelper
 * Description:    数据库帮助类 2.2 版
 * Author:         zhaopeng
 * CreateDate:     2019/9/21 20:22
 */
public class MusicDatabaseHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;

    public static final String MUSIC_DB = "music.db";                       //音乐记录数据库

    public static final String MUSIC_INFO = "music";                        //应用内音乐记录表
    public static final String MUSIC_DOWNLOAD = "download";                 //下载项表
    public static final String MUSIC_PLAYED = "played";                     //最近播放表
    public static final String MUSIC_FAVOURITE = "favourite";               //我喜欢音乐表
    public static final String MUSIC_COLLECTION = "collection";             //我的收藏
    public static final String MUSIC_MV_TABLE = "mv";                       //mv表
    public static final String MUSIC_LIST = "list";                         //歌单表
    public static final String PLAY_LIST = "playlist";                      //播放列表

    private static final String CREATE_MUSIC_TABLE = "create table music(" +
            "id text primary key  NOT NULL UNIQUE," +
            "name	text," +
            "singer	text," +
            "path	text," +
            "url	text," +
            "album	text)";

    private static final String CREATE_MUSIC_DOWNLOAD_TABLE = "create table download(" +
            "id text primary key  NOT NULL UNIQUE," +
            "name	text," +
            "singer	text," +
            "path	text," +
            "url	text," +
            "success 	integer default 0," +
            "is_download	integer)";

    private static final String CREATE_MUSIC_PLAYED_TABLE = "create table played(" +
            "id text primary key  NOT NULL UNIQUE," +
            "name	text," +
            "singer	text," +
            "path	text," +
            "url	text," +
            "album	text," +
            "last_played long)";

    private static final String CREATE_MUSIC_FAVOURITE_TABLE = "create table favourite(" +
            "id text primary key  NOT NULL UNIQUE," +
            "name	text," +
            "singer	text," +
            "path	text)";

    private static final String CREATE_MUSIC_COLLECTION_TABLE = "create table collection(" +
            "id text primary key  NOT NULL UNIQUE," +
            "title	text," +
            "description text," +
            "kind	integer)";

    private static final String CREATE_MUSIC_MV_TABLE = "create table mv(" +
            "id text primary key  NOT NULL UNIQUE," +
            "name	text," +
            "pictureurl	text)";

    private static final String CREATE_MUSIC_LIST_TABLE = "create table list(" +
            "name text primary key  NOT NULL UNIQUE," +
            "description	text," +
            "type integer," +
            "number	integer," +
            "id text NOT NULL," +
            "creator text," +
            "coverimgurl text," +
            "songs	text)";

    private static final String CREATE_PLAY_LIST_TABLE = "create table playlist(" +
            "id text primary key  NOT NULL UNIQUE," +
            "name	text," +
            "singer	text," +
            "path	text)";

    private static MusicDatabaseHelper mDBHelper = null;

    /**
     * 只能采用懒汉模式。因为饿汉模式static初始化，不能获得Context对象
     *
     * @return
     */
    public static synchronized MusicDatabaseHelper getInstance(Context context) {
        if (mDBHelper == null) {
            mDBHelper = new MusicDatabaseHelper(context, MUSIC_DB, null, DB_VERSION);
        }
        return mDBHelper;
    }

    private MusicDatabaseHelper(Context context, int version) {
        super(context, MUSIC_DB, null, version);
    }

    private MusicDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static int getDbVersion() {
        return DB_VERSION;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MUSIC_TABLE);
        db.execSQL(CREATE_MUSIC_DOWNLOAD_TABLE);
        db.execSQL(CREATE_MUSIC_PLAYED_TABLE);
        db.execSQL(CREATE_MUSIC_FAVOURITE_TABLE);
        db.execSQL(CREATE_MUSIC_COLLECTION_TABLE);
        db.execSQL(CREATE_MUSIC_MV_TABLE);
        db.execSQL(CREATE_MUSIC_LIST_TABLE);
        db.execSQL(CREATE_PLAY_LIST_TABLE);
        //预设数据
        db.execSQL("INSERT INTO mv VALUES(10892907,\"我和我的祖国\",\"http://p4.music.126.net/dQGecKaFKeYCLihGd-dmZw==/109951164383180334.jpg\")");
        db.execSQL("INSERT INTO list VALUES(\"我的歌单\",\"默认歌单\",0,0,-1,\"1571713906139\",null,\"\")");
//        db.execSQL("INSERT INTO list VALUES(\"经典老歌,久听不厌\",\"流年忧光影\",1,115,988690134,null,\"http://p1.music.126.net/VFd5cboNTbnYsWZ5DBn9bg==/18953381440004340.jpg\",null)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //not used
    }
}
