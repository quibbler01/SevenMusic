package com.quibbler.sevenmusic.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;

import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.utils.MusicDatabaseHelper;

import static com.quibbler.sevenmusic.utils.MusicDatabaseHelper.MUSIC_MV_TABLE;

/**
 * Package:        com.quibbler.sevenmusic.contentprovider
 * ClassName:      MusicContentProvider
 * Description:    ContentProvider类，提供音乐数据的访问
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 20:05
 */
public class MusicContentProvider extends ContentProvider {
    private static final int MUSIC_ALL = 0;
    private static final int MUSIC_ALL_ITEM = 1;
    private static final int MUSIC_LOCAL = 2;
    private static final int MUSIC_LOCAL_ITEM = 3;
    private static final int MUSIC_DOWNLOAD = 4;
    private static final int MUSIC_DOWNLOAD_ITEM = 5;
    private static final int MUSIC_PLAYED = 6;
    private static final int MUSIC_PLAYED_ITEM = 7;
    private static final int MUSIC_FAVOURITE = 8;
    private static final int MUSIC_FAVOURITE_ITEM = 9;
    private static final int MUSIC_COLLECTION = 10;
    private static final int MUSIC_COLLECTION_ITEM = 11;
    private static final int MUSIC_MV = 12;
    private static final int MUSIC_MV_ITEM = 13;
    private static final int MUSIC_LIST = 14;
    private static final int MUSIC_LIST_ITEM = 15;
    private static final int PLAY_LIST = 16;
    private static final int PLAY_LIST_ITEM = 17;

    public static final String MUSIC_AUTHORITY = "quibbler.sevenmusic.com.provider";

    public static Uri DOWNLOAD_URL = Uri.parse("content://" + MUSIC_AUTHORITY + "/download");
    public static Uri PLAYED_URL = Uri.parse("content://" + MUSIC_AUTHORITY + "/played");
    public static Uri FAVOURITE_URL = Uri.parse("content://" + MUSIC_AUTHORITY + "/favourite");
    public static Uri COLLECTION_URL = Uri.parse("content://" + MUSIC_AUTHORITY + "/collection");
    public static Uri MV_URL = Uri.parse("content://" + MUSIC_AUTHORITY + "/mv");
    public static Uri SONGLIST_URL = Uri.parse("content://" + MUSIC_AUTHORITY + "/list");
    public static Uri PLAYLIST_URL = Uri.parse("content://" + MUSIC_AUTHORITY + "/playlist");

    private static UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(MUSIC_AUTHORITY, "music", MUSIC_ALL);
        mUriMatcher.addURI(MUSIC_AUTHORITY, "music/#", MUSIC_ALL_ITEM);
        mUriMatcher.addURI(MUSIC_AUTHORITY, "local", MUSIC_LOCAL);
        mUriMatcher.addURI(MUSIC_AUTHORITY, "local/#", MUSIC_LOCAL_ITEM);
        mUriMatcher.addURI(MUSIC_AUTHORITY, "download", MUSIC_DOWNLOAD);
        mUriMatcher.addURI(MUSIC_AUTHORITY, "download/#", MUSIC_DOWNLOAD_ITEM);
        mUriMatcher.addURI(MUSIC_AUTHORITY, "played", MUSIC_PLAYED);
        mUriMatcher.addURI(MUSIC_AUTHORITY, "played/#", MUSIC_PLAYED_ITEM);
        mUriMatcher.addURI(MUSIC_AUTHORITY, "favourite", MUSIC_FAVOURITE);
        mUriMatcher.addURI(MUSIC_AUTHORITY, "favourite/#", MUSIC_FAVOURITE_ITEM);
        mUriMatcher.addURI(MUSIC_AUTHORITY, "collection", MUSIC_COLLECTION);
        mUriMatcher.addURI(MUSIC_AUTHORITY, "collection/#", MUSIC_COLLECTION_ITEM);
        mUriMatcher.addURI(MUSIC_AUTHORITY, "mv", MUSIC_MV);
        mUriMatcher.addURI(MUSIC_AUTHORITY, "mv/#", MUSIC_MV_ITEM);
        mUriMatcher.addURI(MUSIC_AUTHORITY, "list", MUSIC_LIST);
        mUriMatcher.addURI(MUSIC_AUTHORITY, "list/#", MUSIC_LIST_ITEM);
        mUriMatcher.addURI(MUSIC_AUTHORITY, "playlist", PLAY_LIST);
        mUriMatcher.addURI(MUSIC_AUTHORITY, "playlist/#", PLAY_LIST_ITEM);
    }

    private MusicDatabaseHelper mDBHelper;

    public MusicContentProvider() {
    }

    @Override
    public boolean onCreate() {
        mDBHelper = MusicDatabaseHelper.getInstance(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case MUSIC_ALL:
                return "vnd.android.cursor.dir/vnd.quibbler.sevenmusic.com.provider.music";
            case MUSIC_ALL_ITEM:
                return "vnd.android.cursor.item/vnd.quibbler.sevenmusic.com.provider.music";
            case MUSIC_DOWNLOAD:
                return "vnd.android.cursor.dir/vnd.quibbler.sevenmusic.com.provider.download";
            case MUSIC_DOWNLOAD_ITEM:
                return "vnd.android.cursor.item/vnd.quibbler.sevenmusic.com.provider.download";
            case MUSIC_PLAYED:
                return "vnd.android.cursor.dir/vnd.quibbler.sevenmusic.com.provider.played";
            case MUSIC_PLAYED_ITEM:
                return "vnd.android.cursor.item/vnd.quibbler.sevenmusic.com.provider.played";
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int deleteRow = 0;
        switch (mUriMatcher.match(uri)) {
            case MUSIC_ALL:
                deleteRow = db.delete(MusicDatabaseHelper.MUSIC_INFO, selection, selectionArgs);
                break;
            case MUSIC_ALL_ITEM:
                String musicID = uri.getPathSegments().get(1);
                deleteRow = db.delete(MusicDatabaseHelper.MUSIC_INFO, "id = ?", new String[]{musicID});
                break;
            case MUSIC_DOWNLOAD:
                deleteRow = db.delete(MusicDatabaseHelper.MUSIC_DOWNLOAD, selection, selectionArgs);
                break;
            case MUSIC_DOWNLOAD_ITEM:
                String downloadMusicID = uri.getPathSegments().get(1);
                deleteRow = db.delete(MusicDatabaseHelper.MUSIC_DOWNLOAD, "id = ?", new String[]{downloadMusicID});
                break;
            case MUSIC_PLAYED:
                deleteRow = db.delete(MusicDatabaseHelper.MUSIC_PLAYED, selection, selectionArgs);
                break;
            case MUSIC_PLAYED_ITEM:
                String playedMusicID = uri.getPathSegments().get(1);
                deleteRow = db.delete(MusicDatabaseHelper.MUSIC_PLAYED, "id = ?", new String[]{playedMusicID});
                break;
            case MUSIC_FAVOURITE:
                deleteRow = db.delete(MusicDatabaseHelper.MUSIC_FAVOURITE, selection, selectionArgs);
                break;
            case MUSIC_FAVOURITE_ITEM:
                String favouriteMusicID = uri.getPathSegments().get(1);
                deleteRow = db.delete(MusicDatabaseHelper.MUSIC_FAVOURITE, "id = ?", new String[]{favouriteMusicID});
                break;
            case MUSIC_COLLECTION:
                deleteRow = db.delete(MusicDatabaseHelper.MUSIC_COLLECTION, selection, selectionArgs);
                break;
            case MUSIC_COLLECTION_ITEM:
                String collectionID = uri.getPathSegments().get(1);
                deleteRow = db.delete(MusicDatabaseHelper.MUSIC_COLLECTION, "id = ?", new String[]{collectionID});
                break;
            case MUSIC_LIST:
                deleteRow = db.delete(MusicDatabaseHelper.MUSIC_LIST, selection, selectionArgs);
                break;
            case MUSIC_LIST_ITEM:
                String songListName = uri.getPathSegments().get(1);
                deleteRow = db.delete(MusicDatabaseHelper.MUSIC_LIST, "name = ?", new String[]{songListName});
                break;
            case PLAY_LIST:
                deleteRow = db.delete(MusicDatabaseHelper.PLAY_LIST, selection, selectionArgs);
                break;
            case PLAY_LIST_ITEM:
                String musicPlayID = uri.getPathSegments().get(1);
                deleteRow = db.delete(MusicDatabaseHelper.PLAY_LIST, "id = ?", new String[]{musicPlayID});
                break;
            case MUSIC_MV:
                deleteRow = db.delete(MUSIC_MV_TABLE, selection, selectionArgs);
                break;
            case MUSIC_MV_ITEM:
                String mvID = uri.getPathSegments().get(1);
                deleteRow = db.delete(MUSIC_MV_TABLE, "id = ?", new String[]{mvID});
                break;
            default:
                break;
        }
        return deleteRow;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Uri uriReturn = null;
        switch (mUriMatcher.match(uri)) {
            case MUSIC_ALL:
            case MUSIC_ALL_ITEM:
                long newMusicId = db.insert(MusicDatabaseHelper.MUSIC_INFO, null, values);
                uriReturn = Uri.parse("content://" + MUSIC_AUTHORITY + "/music/" + newMusicId);
                break;
            case MUSIC_LOCAL:
            case MUSIC_LOCAL_ITEM:
                //系统媒体库
                break;
            case MUSIC_DOWNLOAD:
            case MUSIC_DOWNLOAD_ITEM:
                long downloadMusicId = db.insert(MusicDatabaseHelper.MUSIC_DOWNLOAD, null, values);
                uriReturn = Uri.parse("content://" + MUSIC_AUTHORITY + "/music/" + downloadMusicId);
                break;
            case MUSIC_PLAYED:
            case MUSIC_PLAYED_ITEM:
                long playedMusicID = db.insert(MusicDatabaseHelper.MUSIC_PLAYED, null, values);
                uriReturn = Uri.parse("content://" + MUSIC_AUTHORITY + "/music/" + playedMusicID);
                break;
            case MUSIC_FAVOURITE:
            case MUSIC_FAVOURITE_ITEM:
                long favouriteMusicID = db.insert(MusicDatabaseHelper.MUSIC_FAVOURITE, null, values);
                uriReturn = Uri.parse("content://" + MUSIC_AUTHORITY + "/music/" + favouriteMusicID);
                break;
            case MUSIC_COLLECTION:
            case MUSIC_COLLECTION_ITEM:
                long collectionID = db.insert(MusicDatabaseHelper.MUSIC_COLLECTION, null, values);
                uriReturn = Uri.parse("content://" + MUSIC_COLLECTION + "/collection/" + collectionID);
                break;
            case MUSIC_LIST:
            case MUSIC_LIST_ITEM:
                long songListName = db.insert(MusicDatabaseHelper.MUSIC_LIST, null, values);
                uriReturn = Uri.parse("content://" + MUSIC_LIST + "/list/" + songListName);
                break;
            case PLAY_LIST:
            case PLAY_LIST_ITEM:
                long playMusicID = db.insert(MusicDatabaseHelper.PLAY_LIST, null, values);
                uriReturn = Uri.parse("content://" + PLAY_LIST + "/list/" + playMusicID);
                break;
            case MUSIC_MV:
            case MUSIC_MV_ITEM:
                long mvID = db.insert(MUSIC_MV_TABLE, null, values);
                uriReturn = Uri.parse("content://" + MUSIC_MV_TABLE + "/mv/" + mvID);
                break;
            default:
                break;
        }
        return uriReturn;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (mUriMatcher.match(uri)) {
            case MUSIC_ALL:
                cursor = db.query(MusicDatabaseHelper.MUSIC_INFO, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MUSIC_ALL_ITEM:
                String musicID = uri.getPathSegments().get(1);
                cursor = db.query(MusicDatabaseHelper.MUSIC_INFO, projection, "id = ?", new String[]{musicID}, null, null, sortOrder);
                break;
            case MUSIC_LOCAL:
                cursor = MusicApplication.getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                break;
            case MUSIC_LOCAL_ITEM:
                String localMusicID = uri.getPathSegments().get(1);
                cursor = MusicApplication.getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, "id = ?", new String[]{localMusicID}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                break;
            case MUSIC_DOWNLOAD:
                cursor = db.query(MusicDatabaseHelper.MUSIC_DOWNLOAD, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MUSIC_DOWNLOAD_ITEM:
                String downloadMusicID = uri.getPathSegments().get(1);
                cursor = db.query(MusicDatabaseHelper.MUSIC_DOWNLOAD, projection, "id = ?", new String[]{downloadMusicID}, null, null, sortOrder);
                break;
            case MUSIC_PLAYED:
                cursor = db.query(MusicDatabaseHelper.MUSIC_PLAYED, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MUSIC_PLAYED_ITEM:
                String playedMusicID = uri.getPathSegments().get(1);
                cursor = db.query(MusicDatabaseHelper.MUSIC_PLAYED, projection, "id = ?", new String[]{playedMusicID}, null, null, sortOrder);
                break;
            case MUSIC_FAVOURITE:
                cursor = db.query(MusicDatabaseHelper.MUSIC_FAVOURITE, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MUSIC_FAVOURITE_ITEM:
                String favouriteMusicID = uri.getPathSegments().get(1);
                cursor = db.query(MusicDatabaseHelper.MUSIC_FAVOURITE, projection, "id = ?", new String[]{favouriteMusicID}, null, null, sortOrder);
                break;
            case MUSIC_COLLECTION:
                cursor = db.query(MusicDatabaseHelper.MUSIC_COLLECTION, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MUSIC_COLLECTION_ITEM:
                String collectionID = uri.getPathSegments().get(1);
                cursor = db.query(MusicDatabaseHelper.MUSIC_COLLECTION, projection, "id = ?", new String[]{collectionID}, null, null, sortOrder);
                break;
            case MUSIC_LIST:
                cursor = db.query(MusicDatabaseHelper.MUSIC_LIST, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MUSIC_LIST_ITEM:
                String songListName = uri.getPathSegments().get(1);
                cursor = db.query(MusicDatabaseHelper.MUSIC_LIST, projection, "name = ?", new String[]{songListName}, null, null, sortOrder);
                break;
            case PLAY_LIST:
                cursor = db.query(MusicDatabaseHelper.PLAY_LIST, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PLAY_LIST_ITEM:
                String musicPlayID = uri.getPathSegments().get(1);
                cursor = db.query(MusicDatabaseHelper.PLAY_LIST, projection, "id = ?", new String[]{musicPlayID}, null, null, sortOrder);
                break;
            case MUSIC_MV:
                cursor = db.query(MusicDatabaseHelper.MUSIC_MV_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MUSIC_MV_ITEM:
                String mvID = uri.getPathSegments().get(1);
                cursor = db.query(MusicDatabaseHelper.MUSIC_MV_TABLE, projection, "id = ?", new String[]{mvID}, null, null, sortOrder);
                break;
            default:
                break;
        }

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int updateRows = 0;
        switch (mUriMatcher.match(uri)) {
            case MUSIC_ALL:
                updateRows = db.update(MusicDatabaseHelper.MUSIC_INFO, values, selection, selectionArgs);
                break;
            case MUSIC_ALL_ITEM:
                String musicID = uri.getPathSegments().get(1);
                updateRows = db.update(MusicDatabaseHelper.MUSIC_INFO, values, "id = ?", new String[]{musicID});
                break;
            case MUSIC_LOCAL:
            case MUSIC_LOCAL_ITEM:
                //系统媒体库
                break;
            case MUSIC_DOWNLOAD:
                updateRows = db.update(MusicDatabaseHelper.MUSIC_DOWNLOAD, values, selection, selectionArgs);
                break;
            case MUSIC_DOWNLOAD_ITEM:
                String downloadMusicID = uri.getPathSegments().get(1);
                updateRows = db.update(MusicDatabaseHelper.MUSIC_DOWNLOAD, values, "id = ?", new String[]{downloadMusicID});
                break;
            case MUSIC_PLAYED:
                updateRows = db.update(MusicDatabaseHelper.MUSIC_PLAYED, values, selection, selectionArgs);
                break;
            case MUSIC_PLAYED_ITEM:
                String playedMusicID = uri.getPathSegments().get(1);
                updateRows = db.update(MusicDatabaseHelper.MUSIC_PLAYED, values, "id = ?", new String[]{playedMusicID});
                break;
            case MUSIC_FAVOURITE:
                updateRows = db.update(MusicDatabaseHelper.MUSIC_FAVOURITE, values, selection, selectionArgs);
                break;
            case MUSIC_FAVOURITE_ITEM:
                String favouriteMusicID = uri.getPathSegments().get(1);
                updateRows = db.update(MusicDatabaseHelper.MUSIC_FAVOURITE, values, "id = ?", new String[]{favouriteMusicID});
                break;
            case MUSIC_COLLECTION:
                updateRows = db.update(MusicDatabaseHelper.MUSIC_COLLECTION, values, selection, selectionArgs);
                break;
            case MUSIC_COLLECTION_ITEM:
                String collectionID = uri.getPathSegments().get(1);
                updateRows = db.update(MusicDatabaseHelper.MUSIC_COLLECTION, values, "id = ?", new String[]{collectionID});
                break;
            case MUSIC_LIST:
                updateRows = db.update(MusicDatabaseHelper.MUSIC_LIST, values, selection, selectionArgs);
                break;
            case MUSIC_LIST_ITEM:
                String songListName = uri.getPathSegments().get(1);
                updateRows = db.update(MusicDatabaseHelper.MUSIC_LIST, values, "name = ?", new String[]{songListName});
                break;
            case PLAY_LIST:
                updateRows = db.update(MusicDatabaseHelper.PLAY_LIST, values, selection, selectionArgs);
                break;
            case PLAY_LIST_ITEM:
                String musicPlayID = uri.getPathSegments().get(1);
                updateRows = db.update(MusicDatabaseHelper.PLAY_LIST, values, "id = ?", new String[]{musicPlayID});
                break;
            case MUSIC_MV:
                updateRows = db.update(MusicDatabaseHelper.MUSIC_MV_TABLE, values, selection, selectionArgs);
                break;
            case MUSIC_MV_ITEM:
                String mvID = uri.getPathSegments().get(1);
                updateRows = db.update(MusicDatabaseHelper.MUSIC_MV_TABLE, values, "id = ?", new String[]{mvID});
                break;
            default:
                break;
        }
        return updateRows;
    }
}
