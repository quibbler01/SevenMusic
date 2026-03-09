package com.quibbler.sevenmusic.contentprovider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.utils.MusicDatabaseHelper

/**
 * Package:        com.quibbler.sevenmusic.contentprovider
 * ClassName:      MusicContentProvider
 * Description:    ContentProvider类，提供音乐数据的访问
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 20:05
 */
class MusicContentProvider : ContentProvider() {
    private var mDBHelper: MusicDatabaseHelper? = null

    override fun onCreate(): Boolean {
        mDBHelper = MusicDatabaseHelper.Companion.getInstance(getContext())
        return true
    }

    override fun getType(uri: Uri?): String? {
        when (mUriMatcher.match(uri)) {
            MUSIC_ALL -> return "vnd.android.cursor.dir/vnd.quibbler.sevenmusic.com.provider.music"
            MUSIC_ALL_ITEM -> return "vnd.android.cursor.item/vnd.quibbler.sevenmusic.com.provider.music"
            MUSIC_DOWNLOAD -> return "vnd.android.cursor.dir/vnd.quibbler.sevenmusic.com.provider.download"
            MUSIC_DOWNLOAD_ITEM -> return "vnd.android.cursor.item/vnd.quibbler.sevenmusic.com.provider.download"
            MUSIC_PLAYED -> return "vnd.android.cursor.dir/vnd.quibbler.sevenmusic.com.provider.played"
            MUSIC_PLAYED_ITEM -> return "vnd.android.cursor.item/vnd.quibbler.sevenmusic.com.provider.played"
        }
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String?>?): Int {
        val db = mDBHelper!!.getWritableDatabase()
        var deleteRow = 0
        when (mUriMatcher.match(uri)) {
            MUSIC_ALL -> deleteRow =
                db.delete(MusicDatabaseHelper.Companion.MUSIC_INFO, selection, selectionArgs)

            MUSIC_ALL_ITEM -> {
                val musicID = uri.getPathSegments().get(1)
                deleteRow = db.delete(
                    MusicDatabaseHelper.Companion.MUSIC_INFO,
                    "id = ?",
                    arrayOf<String?>(musicID)
                )
            }

            MUSIC_DOWNLOAD -> deleteRow =
                db.delete(MusicDatabaseHelper.Companion.MUSIC_DOWNLOAD, selection, selectionArgs)

            MUSIC_DOWNLOAD_ITEM -> {
                val downloadMusicID = uri.getPathSegments().get(1)
                deleteRow = db.delete(
                    MusicDatabaseHelper.Companion.MUSIC_DOWNLOAD,
                    "id = ?",
                    arrayOf<String?>(downloadMusicID)
                )
            }

            MUSIC_PLAYED -> deleteRow =
                db.delete(MusicDatabaseHelper.Companion.MUSIC_PLAYED, selection, selectionArgs)

            MUSIC_PLAYED_ITEM -> {
                val playedMusicID = uri.getPathSegments().get(1)
                deleteRow = db.delete(
                    MusicDatabaseHelper.Companion.MUSIC_PLAYED,
                    "id = ?",
                    arrayOf<String?>(playedMusicID)
                )
            }

            MUSIC_FAVOURITE -> deleteRow =
                db.delete(MusicDatabaseHelper.Companion.MUSIC_FAVOURITE, selection, selectionArgs)

            MUSIC_FAVOURITE_ITEM -> {
                val favouriteMusicID = uri.getPathSegments().get(1)
                deleteRow = db.delete(
                    MusicDatabaseHelper.Companion.MUSIC_FAVOURITE,
                    "id = ?",
                    arrayOf<String?>(favouriteMusicID)
                )
            }

            MUSIC_COLLECTION -> deleteRow =
                db.delete(MusicDatabaseHelper.Companion.MUSIC_COLLECTION, selection, selectionArgs)

            MUSIC_COLLECTION_ITEM -> {
                val collectionID = uri.getPathSegments().get(1)
                deleteRow = db.delete(
                    MusicDatabaseHelper.Companion.MUSIC_COLLECTION,
                    "id = ?",
                    arrayOf<String?>(collectionID)
                )
            }

            MUSIC_LIST -> deleteRow =
                db.delete(MusicDatabaseHelper.Companion.MUSIC_LIST, selection, selectionArgs)

            MUSIC_LIST_ITEM -> {
                val songListName = uri.getPathSegments().get(1)
                deleteRow = db.delete(
                    MusicDatabaseHelper.Companion.MUSIC_LIST,
                    "name = ?",
                    arrayOf<String?>(songListName)
                )
            }

            PLAY_LIST -> deleteRow =
                db.delete(MusicDatabaseHelper.Companion.PLAY_LIST, selection, selectionArgs)

            PLAY_LIST_ITEM -> {
                val musicPlayID = uri.getPathSegments().get(1)
                deleteRow = db.delete(
                    MusicDatabaseHelper.Companion.PLAY_LIST,
                    "id = ?",
                    arrayOf<String?>(musicPlayID)
                )
            }

            MUSIC_MV -> deleteRow =
                db.delete(MusicDatabaseHelper.Companion.MUSIC_MV_TABLE, selection, selectionArgs)

            MUSIC_MV_ITEM -> {
                val mvID = uri.getPathSegments().get(1)
                deleteRow = db.delete(
                    MusicDatabaseHelper.Companion.MUSIC_MV_TABLE,
                    "id = ?",
                    arrayOf<String?>(mvID)
                )
            }

            else -> {}
        }
        return deleteRow
    }

    override fun insert(uri: Uri?, values: ContentValues?): Uri? {
        val db = mDBHelper!!.getWritableDatabase()
        var uriReturn: Uri? = null
        when (mUriMatcher.match(uri)) {
            MUSIC_ALL, MUSIC_ALL_ITEM -> {
                val newMusicId = db.insert(MusicDatabaseHelper.Companion.MUSIC_INFO, null, values)
                uriReturn = Uri.parse("content://" + MUSIC_AUTHORITY + "/music/" + newMusicId)
            }

            MUSIC_LOCAL, MUSIC_LOCAL_ITEM -> {}
            MUSIC_DOWNLOAD, MUSIC_DOWNLOAD_ITEM -> {
                val downloadMusicId =
                    db.insert(MusicDatabaseHelper.Companion.MUSIC_DOWNLOAD, null, values)
                uriReturn = Uri.parse("content://" + MUSIC_AUTHORITY + "/music/" + downloadMusicId)
            }

            MUSIC_PLAYED, MUSIC_PLAYED_ITEM -> {
                val playedMusicID =
                    db.insert(MusicDatabaseHelper.Companion.MUSIC_PLAYED, null, values)
                uriReturn = Uri.parse("content://" + MUSIC_AUTHORITY + "/music/" + playedMusicID)
            }

            MUSIC_FAVOURITE, MUSIC_FAVOURITE_ITEM -> {
                val favouriteMusicID =
                    db.insert(MusicDatabaseHelper.Companion.MUSIC_FAVOURITE, null, values)
                uriReturn = Uri.parse("content://" + MUSIC_AUTHORITY + "/music/" + favouriteMusicID)
            }

            MUSIC_COLLECTION, MUSIC_COLLECTION_ITEM -> {
                val collectionID =
                    db.insert(MusicDatabaseHelper.Companion.MUSIC_COLLECTION, null, values)
                uriReturn =
                    Uri.parse("content://" + MUSIC_COLLECTION + "/collection/" + collectionID)
            }

            MUSIC_LIST, MUSIC_LIST_ITEM -> {
                val songListName = db.insert(MusicDatabaseHelper.Companion.MUSIC_LIST, null, values)
                uriReturn = Uri.parse("content://" + MUSIC_LIST + "/list/" + songListName)
            }

            PLAY_LIST, PLAY_LIST_ITEM -> {
                val playMusicID = db.insert(MusicDatabaseHelper.Companion.PLAY_LIST, null, values)
                uriReturn = Uri.parse("content://" + PLAY_LIST + "/list/" + playMusicID)
            }

            MUSIC_MV, MUSIC_MV_ITEM -> {
                val mvID = db.insert(MusicDatabaseHelper.Companion.MUSIC_MV_TABLE, null, values)
                uriReturn =
                    Uri.parse("content://" + MusicDatabaseHelper.Companion.MUSIC_MV_TABLE + "/mv/" + mvID)
            }

            else -> {}
        }
        return uriReturn
    }

    override fun query(
        uri: Uri,
        projection: Array<String?>?,
        selection: String?,
        selectionArgs: Array<String?>?,
        sortOrder: String?
    ): Cursor? {
        val db = mDBHelper!!.getReadableDatabase()
        var cursor: Cursor? = null
        when (mUriMatcher.match(uri)) {
            MUSIC_ALL -> cursor = db.query(
                MusicDatabaseHelper.Companion.MUSIC_INFO,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )

            MUSIC_ALL_ITEM -> {
                val musicID = uri.getPathSegments().get(1)
                cursor = db.query(
                    MusicDatabaseHelper.Companion.MUSIC_INFO,
                    projection,
                    "id = ?",
                    arrayOf<String?>(musicID),
                    null,
                    null,
                    sortOrder
                )
            }

            MUSIC_LOCAL -> cursor = MusicApplication.Companion.getContext().getContentResolver()
                .query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER
                )

            MUSIC_LOCAL_ITEM -> {
                val localMusicID = uri.getPathSegments().get(1)
                cursor = MusicApplication.Companion.getContext().getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    "id = ?",
                    arrayOf<String?>(localMusicID),
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER
                )
            }

            MUSIC_DOWNLOAD -> cursor = db.query(
                MusicDatabaseHelper.Companion.MUSIC_DOWNLOAD,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )

            MUSIC_DOWNLOAD_ITEM -> {
                val downloadMusicID = uri.getPathSegments().get(1)
                cursor = db.query(
                    MusicDatabaseHelper.Companion.MUSIC_DOWNLOAD,
                    projection,
                    "id = ?",
                    arrayOf<String?>(downloadMusicID),
                    null,
                    null,
                    sortOrder
                )
            }

            MUSIC_PLAYED -> cursor = db.query(
                MusicDatabaseHelper.Companion.MUSIC_PLAYED,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )

            MUSIC_PLAYED_ITEM -> {
                val playedMusicID = uri.getPathSegments().get(1)
                cursor = db.query(
                    MusicDatabaseHelper.Companion.MUSIC_PLAYED,
                    projection,
                    "id = ?",
                    arrayOf<String?>(playedMusicID),
                    null,
                    null,
                    sortOrder
                )
            }

            MUSIC_FAVOURITE -> cursor = db.query(
                MusicDatabaseHelper.Companion.MUSIC_FAVOURITE,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )

            MUSIC_FAVOURITE_ITEM -> {
                val favouriteMusicID = uri.getPathSegments().get(1)
                cursor = db.query(
                    MusicDatabaseHelper.Companion.MUSIC_FAVOURITE,
                    projection,
                    "id = ?",
                    arrayOf<String?>(favouriteMusicID),
                    null,
                    null,
                    sortOrder
                )
            }

            MUSIC_COLLECTION -> cursor = db.query(
                MusicDatabaseHelper.Companion.MUSIC_COLLECTION,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )

            MUSIC_COLLECTION_ITEM -> {
                val collectionID = uri.getPathSegments().get(1)
                cursor = db.query(
                    MusicDatabaseHelper.Companion.MUSIC_COLLECTION,
                    projection,
                    "id = ?",
                    arrayOf<String?>(collectionID),
                    null,
                    null,
                    sortOrder
                )
            }

            MUSIC_LIST -> cursor = db.query(
                MusicDatabaseHelper.Companion.MUSIC_LIST,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )

            MUSIC_LIST_ITEM -> {
                val songListName = uri.getPathSegments().get(1)
                cursor = db.query(
                    MusicDatabaseHelper.Companion.MUSIC_LIST,
                    projection,
                    "name = ?",
                    arrayOf<String?>(songListName),
                    null,
                    null,
                    sortOrder
                )
            }

            PLAY_LIST -> cursor = db.query(
                MusicDatabaseHelper.Companion.PLAY_LIST,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )

            PLAY_LIST_ITEM -> {
                val musicPlayID = uri.getPathSegments().get(1)
                cursor = db.query(
                    MusicDatabaseHelper.Companion.PLAY_LIST,
                    projection,
                    "id = ?",
                    arrayOf<String?>(musicPlayID),
                    null,
                    null,
                    sortOrder
                )
            }

            MUSIC_MV -> cursor = db.query(
                MusicDatabaseHelper.Companion.MUSIC_MV_TABLE,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )

            MUSIC_MV_ITEM -> {
                val mvID = uri.getPathSegments().get(1)
                cursor = db.query(
                    MusicDatabaseHelper.Companion.MUSIC_MV_TABLE,
                    projection,
                    "id = ?",
                    arrayOf<String?>(mvID),
                    null,
                    null,
                    sortOrder
                )
            }

            else -> {}
        }

        return cursor
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String?>?
    ): Int {
        val db = mDBHelper!!.getWritableDatabase()
        var updateRows = 0
        when (mUriMatcher.match(uri)) {
            MUSIC_ALL -> updateRows = db.update(
                MusicDatabaseHelper.Companion.MUSIC_INFO,
                values,
                selection,
                selectionArgs
            )

            MUSIC_ALL_ITEM -> {
                val musicID = uri.getPathSegments().get(1)
                updateRows = db.update(
                    MusicDatabaseHelper.Companion.MUSIC_INFO,
                    values,
                    "id = ?",
                    arrayOf<String?>(musicID)
                )
            }

            MUSIC_LOCAL, MUSIC_LOCAL_ITEM -> {}
            MUSIC_DOWNLOAD -> updateRows = db.update(
                MusicDatabaseHelper.Companion.MUSIC_DOWNLOAD,
                values,
                selection,
                selectionArgs
            )

            MUSIC_DOWNLOAD_ITEM -> {
                val downloadMusicID = uri.getPathSegments().get(1)
                updateRows = db.update(
                    MusicDatabaseHelper.Companion.MUSIC_DOWNLOAD,
                    values,
                    "id = ?",
                    arrayOf<String?>(downloadMusicID)
                )
            }

            MUSIC_PLAYED -> updateRows = db.update(
                MusicDatabaseHelper.Companion.MUSIC_PLAYED,
                values,
                selection,
                selectionArgs
            )

            MUSIC_PLAYED_ITEM -> {
                val playedMusicID = uri.getPathSegments().get(1)
                updateRows = db.update(
                    MusicDatabaseHelper.Companion.MUSIC_PLAYED,
                    values,
                    "id = ?",
                    arrayOf<String?>(playedMusicID)
                )
            }

            MUSIC_FAVOURITE -> updateRows = db.update(
                MusicDatabaseHelper.Companion.MUSIC_FAVOURITE,
                values,
                selection,
                selectionArgs
            )

            MUSIC_FAVOURITE_ITEM -> {
                val favouriteMusicID = uri.getPathSegments().get(1)
                updateRows = db.update(
                    MusicDatabaseHelper.Companion.MUSIC_FAVOURITE,
                    values,
                    "id = ?",
                    arrayOf<String?>(favouriteMusicID)
                )
            }

            MUSIC_COLLECTION -> updateRows = db.update(
                MusicDatabaseHelper.Companion.MUSIC_COLLECTION,
                values,
                selection,
                selectionArgs
            )

            MUSIC_COLLECTION_ITEM -> {
                val collectionID = uri.getPathSegments().get(1)
                updateRows = db.update(
                    MusicDatabaseHelper.Companion.MUSIC_COLLECTION,
                    values,
                    "id = ?",
                    arrayOf<String?>(collectionID)
                )
            }

            MUSIC_LIST -> updateRows = db.update(
                MusicDatabaseHelper.Companion.MUSIC_LIST,
                values,
                selection,
                selectionArgs
            )

            MUSIC_LIST_ITEM -> {
                val songListName = uri.getPathSegments().get(1)
                updateRows = db.update(
                    MusicDatabaseHelper.Companion.MUSIC_LIST,
                    values,
                    "name = ?",
                    arrayOf<String?>(songListName)
                )
            }

            PLAY_LIST -> updateRows =
                db.update(MusicDatabaseHelper.Companion.PLAY_LIST, values, selection, selectionArgs)

            PLAY_LIST_ITEM -> {
                val musicPlayID = uri.getPathSegments().get(1)
                updateRows = db.update(
                    MusicDatabaseHelper.Companion.PLAY_LIST,
                    values,
                    "id = ?",
                    arrayOf<String?>(musicPlayID)
                )
            }

            MUSIC_MV -> updateRows = db.update(
                MusicDatabaseHelper.Companion.MUSIC_MV_TABLE,
                values,
                selection,
                selectionArgs
            )

            MUSIC_MV_ITEM -> {
                val mvID = uri.getPathSegments().get(1)
                updateRows = db.update(
                    MusicDatabaseHelper.Companion.MUSIC_MV_TABLE,
                    values,
                    "id = ?",
                    arrayOf<String?>(mvID)
                )
            }

            else -> {}
        }
        return updateRows
    }

    companion object {
        private const val MUSIC_ALL = 0
        private const val MUSIC_ALL_ITEM = 1
        private const val MUSIC_LOCAL = 2
        private const val MUSIC_LOCAL_ITEM = 3
        private const val MUSIC_DOWNLOAD = 4
        private const val MUSIC_DOWNLOAD_ITEM = 5
        private const val MUSIC_PLAYED = 6
        private const val MUSIC_PLAYED_ITEM = 7
        private const val MUSIC_FAVOURITE = 8
        private const val MUSIC_FAVOURITE_ITEM = 9
        private const val MUSIC_COLLECTION = 10
        private const val MUSIC_COLLECTION_ITEM = 11
        private const val MUSIC_MV = 12
        private const val MUSIC_MV_ITEM = 13
        private const val MUSIC_LIST = 14
        private const val MUSIC_LIST_ITEM = 15
        private const val PLAY_LIST = 16
        private const val PLAY_LIST_ITEM = 17

        const val MUSIC_AUTHORITY: String = "quibbler.sevenmusic.com.provider"

        var DOWNLOAD_URL: Uri? = Uri.parse("content://" + MUSIC_AUTHORITY + "/download")
        var PLAYED_URL: Uri? = Uri.parse("content://" + MUSIC_AUTHORITY + "/played")
        var FAVOURITE_URL: Uri? = Uri.parse("content://" + MUSIC_AUTHORITY + "/favourite")
        var COLLECTION_URL: Uri? = Uri.parse("content://" + MUSIC_AUTHORITY + "/collection")
        var MV_URL: Uri? = Uri.parse("content://" + MUSIC_AUTHORITY + "/mv")
        var SONGLIST_URL: Uri? = Uri.parse("content://" + MUSIC_AUTHORITY + "/list")
        var PLAYLIST_URL: Uri? = Uri.parse("content://" + MUSIC_AUTHORITY + "/playlist")

        private val mUriMatcher: UriMatcher

        init {
            mUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            mUriMatcher.addURI(MUSIC_AUTHORITY, "music", MUSIC_ALL)
            mUriMatcher.addURI(MUSIC_AUTHORITY, "music/#", MUSIC_ALL_ITEM)
            mUriMatcher.addURI(MUSIC_AUTHORITY, "local", MUSIC_LOCAL)
            mUriMatcher.addURI(MUSIC_AUTHORITY, "local/#", MUSIC_LOCAL_ITEM)
            mUriMatcher.addURI(MUSIC_AUTHORITY, "download", MUSIC_DOWNLOAD)
            mUriMatcher.addURI(MUSIC_AUTHORITY, "download/#", MUSIC_DOWNLOAD_ITEM)
            mUriMatcher.addURI(MUSIC_AUTHORITY, "played", MUSIC_PLAYED)
            mUriMatcher.addURI(MUSIC_AUTHORITY, "played/#", MUSIC_PLAYED_ITEM)
            mUriMatcher.addURI(MUSIC_AUTHORITY, "favourite", MUSIC_FAVOURITE)
            mUriMatcher.addURI(MUSIC_AUTHORITY, "favourite/#", MUSIC_FAVOURITE_ITEM)
            mUriMatcher.addURI(MUSIC_AUTHORITY, "collection", MUSIC_COLLECTION)
            mUriMatcher.addURI(MUSIC_AUTHORITY, "collection/#", MUSIC_COLLECTION_ITEM)
            mUriMatcher.addURI(MUSIC_AUTHORITY, "mv", MUSIC_MV)
            mUriMatcher.addURI(MUSIC_AUTHORITY, "mv/#", MUSIC_MV_ITEM)
            mUriMatcher.addURI(MUSIC_AUTHORITY, "list", MUSIC_LIST)
            mUriMatcher.addURI(MUSIC_AUTHORITY, "list/#", MUSIC_LIST_ITEM)
            mUriMatcher.addURI(MUSIC_AUTHORITY, "playlist", PLAY_LIST)
            mUriMatcher.addURI(MUSIC_AUTHORITY, "playlist/#", PLAY_LIST_ITEM)
        }
    }
}
