package com.quibbler.sevenmusic.broadcast;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.quibbler.sevenmusic.MusicApplication;

/**
 * Package:        com.quibbler.sevenmusic.broadcast
 * ClassName:      MusicBroadcastManager
 * Description:    发送本应用本地全局广播
 * 注意全局的广播只能使用Activity中的registerReceiver()方法进行注册，否则如果使用本地广播管理器注册的话是接收不到全局广播的
 * Author:         zhaopeng
 * CreateDate:     2019/9/27 17:33
 */
public class MusicBroadcastManager {

    public static final String AUDIO_BECOMING_NOISY = "android.media.AUDIO_BECOMING_NOISY";
    public static final String SYSTEM_BROADCAST_NETWORK_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String MUSIC_GLOBAL_PLAY_COMPLETION = "com.quibbler.sevenmusic.music.play.completion";
    public static final String MUSIC_GLOBAL_PLAY = "quibbler.com.sevenmusic.global.play";                               //开始播放通知广播
    public static final String MUSIC_GLOBAL_PAUSE = "quibbler.com.sevenmusic.global.pause";                             //播放暂停通知广播，前台可据此切换状态
    public static final String MUSIC_GLOBAL_NO_COPYRIGHT = "quibbler.com.sevenmusic.global.copyright";                  //没有版权发送一条通知广播
    public static final String MUSIC_GLOBAL_SOMETHING_WRONG = "quibbler.com.sevenmusic.global.something.wrong";         //播放出错通知广播
    public static final String MUSIC_GLOBAL_DATABASE_UPDATE = "quibbler.com.sevenmusic.global.database.update";         //本地数据库变化通知广播
    public static final String MUSIC_GLOBAL_MUSIC_DOWNLOAD_SUCCESS = "quibbler.com.sevenmusic.global.download.success"; //歌曲下载成功
    public static final String MUSIC_GLOBAL_MUSIC_DOWNLOAD_FAILED = "quibbler.com.sevenmusic.global.download.failed";   //歌曲下载失败
    public static final String MUSIC_GLOBAL_TIMING_STOP_PLAY = "com.quibbler.sevenmusic.view.global.timingstopplay";   //定时停止播放通知广播
    public static final String MUSIC_GLOBAL_PLAY_BAR_UPDATE = "com.quibbler.sevenmusic.view.global.playbar";   //音乐播放条播放列表歌曲点击通知广播
    public static final String MUSIC_GLOBAL_MUSIC_PLAY_PROGRESSBAR_UPDATE = "com.quibbler.sevenmusic.update_progressbar_broadcast";   //音乐播放界面进度条更新通知广播

    public static final String MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_ONE = "quibbler.com.sevenmusic.global.main.index.one";          //改变索引
    public static final String MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_TWO = "quibbler.com.sevenmusic.global.main.index.two";          //
    public static final String MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_THREE = "quibbler.com.sevenmusic.global.main.index.three";      //


    private static LocalBroadcastManager mLocalBroadcastManager = LocalBroadcastManager.getInstance(MusicApplication.getContext());

    public static void sendBroadcast(Intent intent) {
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    public static void sendBroadcast(String action) {
        Intent intent = new Intent(action);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    public static void registerMusicBroadcastReceiver(BroadcastReceiver receiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MUSIC_GLOBAL_PLAY);
        intentFilter.addAction(MUSIC_GLOBAL_PAUSE);
        mLocalBroadcastManager.registerReceiver(receiver, intentFilter);
    }

    public static void registerMusicBroadcastReceiver(BroadcastReceiver receiver, String action) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action);
        mLocalBroadcastManager.registerReceiver(receiver, intentFilter);
    }

    public static void registerMusicBroadcastReceiver(BroadcastReceiver receiver, IntentFilter intentFilter) {
        mLocalBroadcastManager.registerReceiver(receiver, intentFilter);
    }

    public static void unregisterMusicBroadcastReceiver(BroadcastReceiver broadcastReceiver) {
        mLocalBroadcastManager.unregisterReceiver(broadcastReceiver);
    }

    public static void registerMusicBroadcastReceiverForMusicIntent(BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MUSIC_GLOBAL_PLAY);
        intentFilter.addAction(MUSIC_GLOBAL_PAUSE);
        intentFilter.addAction(MUSIC_GLOBAL_NO_COPYRIGHT);
        intentFilter.addAction(MUSIC_GLOBAL_SOMETHING_WRONG);
        mLocalBroadcastManager.registerReceiver(broadcastReceiver, intentFilter);
    }
}


