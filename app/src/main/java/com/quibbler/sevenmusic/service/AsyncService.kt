package com.quibbler.sevenmusic.service

import android.Manifest.permission
import android.app.IntentService
import android.content.Intent
import android.provider.MediaStore
import com.quibbler.sevenmusic.utils.CheckTools

/**
 * Package:        com.quibbler.sevenmusic.service
 * ClassName:      AsyncService
 * Description:    后台异步执行,用来预先加载图片，数据，缓存。
 * Author:         zhaopeng
 * CreateDate:     2019/10/31 10:12
 */
class AsyncService : IntentService("AsyncService") {
    override fun onHandleIntent(intent: Intent) {
        if (this.lastAsyncTimeStamp >= System.currentTimeMillis() + MAX_ASYNC_INTERVAL) {
            return
        }
        if (!CheckTools.hasPermission(permission.READ_EXTERNAL_STORAGE, getApplicationContext())) {
            return
        }

        val command = intent.getIntExtra(KEY, -1)
        when (command) {
            COMMAND_SYNC_SINGER -> asyncSingerInfo()
            COMMAND_SYNC_MUSIC -> asyncMusic()
            COMMAND_SYNC_COLLECTION -> asyncCollection()
            else -> {}
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun asyncSingerInfo() {
        lastAsyncTimeStamp()
        val localMusicResolver = getContentResolver()
        val localMusicCursor = localMusicResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        )
        if (localMusicCursor == null) {
            return
        }
        val singers: MutableSet<String?> = HashSet<String?>()
        while (localMusicCursor.moveToNext()) {
            singers.add(localMusicCursor.getString(localMusicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)))
        }
        for (str in singers) {
            val loadSingerThumbnailAsyncTask = LoadSingerThumbnailAsyncTask()
            loadSingerThumbnailAsyncTask.execute(str)
        }
    }

    private fun asyncMusic() {
        //待定
    }

    private fun asyncCollection() {
        //待定
    }

    private fun lastAsyncTimeStamp() {
        val sharedPreferences = getSharedPreferences(NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putLong(TAG, System.currentTimeMillis())
        editor.apply()
    }

    private val lastAsyncTimeStamp: Long
        get() {
            val sharedPreferences = getSharedPreferences(
                NAME,
                MODE_PRIVATE
            )
            return sharedPreferences.getLong(TAG, 0)
        }

    companion object {
        private const val NAME = "AsyncService"
        private const val TAG = "timestamp"
        private const val MAX_ASYNC_INTERVAL: Long = 259200000

        const val KEY: String = "command"

        const val COMMAND_SYNC_SINGER: Int = 0
        const val COMMAND_SYNC_MUSIC: Int = 1
        const val COMMAND_SYNC_COLLECTION: Int = 2
    }
}
