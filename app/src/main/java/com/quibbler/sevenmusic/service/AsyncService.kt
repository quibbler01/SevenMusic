package com.quibbler.sevenmusic.service;

import android.Manifest;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import com.quibbler.sevenmusic.utils.CheckTools;

import java.util.HashSet;
import java.util.Set;

/**
 * Package:        com.quibbler.sevenmusic.service
 * ClassName:      AsyncService
 * Description:    后台异步执行,用来预先加载图片，数据，缓存。
 * Author:         zhaopeng
 * CreateDate:     2019/10/31 10:12
 */
public class AsyncService extends IntentService {
    private static final String NAME = "AsyncService";
    private static final String TAG = "timestamp";
    private static final long MAX_ASYNC_INTERVAL = 259200000;

    public static final String KEY = "command";

    public static final int COMMAND_SYNC_SINGER = 0;
    public static final int COMMAND_SYNC_MUSIC = 1;
    public static final int COMMAND_SYNC_COLLECTION = 2;

    public AsyncService() {
        super("AsyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (getLastAsyncTimeStamp() >= System.currentTimeMillis() + MAX_ASYNC_INTERVAL) {
            return;
        }
        if (!CheckTools.hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE, getApplicationContext())) {
            return;
        }

        int command = intent.getIntExtra(KEY, -1);
        switch (command) {
            case COMMAND_SYNC_SINGER:
                asyncSingerInfo();
                break;
            case COMMAND_SYNC_MUSIC:
                asyncMusic();
                break;
            case COMMAND_SYNC_COLLECTION:
                asyncCollection();
                break;
            default:
                break;
        }

    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void asyncSingerInfo() {
        lastAsyncTimeStamp();
        ContentResolver localMusicResolver = getContentResolver();
        Cursor localMusicCursor = localMusicResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (localMusicCursor == null) {
            return;
        }
        Set<String> singers = new HashSet<>();
        while (localMusicCursor.moveToNext()) {
            singers.add(localMusicCursor.getString(localMusicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
        }
        for (String str : singers) {
            LoadSingerThumbnailAsyncTask loadSingerThumbnailAsyncTask = new LoadSingerThumbnailAsyncTask();
            loadSingerThumbnailAsyncTask.execute(str);
        }
    }

    private void asyncMusic() {
        //待定
    }

    private void asyncCollection() {
        //待定
    }

    private void lastAsyncTimeStamp() {
        SharedPreferences sharedPreferences = getSharedPreferences(NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(TAG, System.currentTimeMillis());
        editor.apply();
    }

    private long getLastAsyncTimeStamp() {
        SharedPreferences sharedPreferences = getSharedPreferences(NAME, MODE_PRIVATE);
        return sharedPreferences.getLong(TAG, 0);
    }
}
